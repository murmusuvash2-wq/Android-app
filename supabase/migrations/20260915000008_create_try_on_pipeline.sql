-- Migration: 20260915000008_create_try_on_pipeline.sql
-- Description: Creates try_on_jobs, try_on_results, private tryon-results storage bucket, and strict RLS policies.

-- =============================================================================
-- 1. TABLE: public.try_on_jobs
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.try_on_jobs (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    product_id text NOT NULL REFERENCES public.products(id) ON DELETE RESTRICT,
    user_photo_storage_path text NOT NULL,
    status text NOT NULL,
    progress numeric,
    provider text,
    error_code text,
    error_message text,
    request_id text,
    created_at timestamptz NOT NULL DEFAULT now(),
    completed_at timestamptz,

    CONSTRAINT try_on_jobs_status_check 
        CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CONSTRAINT try_on_jobs_progress_check 
        CHECK (progress IS NULL OR (progress >= 0 AND progress <= 100))
);

-- Indexes for performance and idempotency
CREATE INDEX IF NOT EXISTS idx_try_on_jobs_user_created 
    ON public.try_on_jobs (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_try_on_jobs_status 
    ON public.try_on_jobs (status);

CREATE INDEX IF NOT EXISTS idx_try_on_jobs_user_request 
    ON public.try_on_jobs (user_id, request_id);

-- =============================================================================
-- 2. TABLE: public.try_on_results
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.try_on_results (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id uuid REFERENCES public.try_on_jobs(id) ON DELETE CASCADE,
    user_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    product_id text NOT NULL REFERENCES public.products(id) ON DELETE RESTRICT,
    result_storage_path text NOT NULL,
    watermark_applied boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_try_on_results_job 
    ON public.try_on_results (job_id);

CREATE INDEX IF NOT EXISTS idx_try_on_results_user_created 
    ON public.try_on_results (user_id, created_at DESC);

-- =============================================================================
-- 3. ROW LEVEL SECURITY (RLS) FOR TABLES
-- =============================================================================
ALTER TABLE public.try_on_jobs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.try_on_results ENABLE ROW LEVEL SECURITY;

-- Clean up any existing policies
DROP POLICY IF EXISTS "user_select_own_try_on_jobs" ON public.try_on_jobs;
DROP POLICY IF EXISTS "user_insert_own_try_on_jobs" ON public.try_on_jobs;
DROP POLICY IF EXISTS "user_update_cancel_own_try_on_jobs" ON public.try_on_jobs;
DROP POLICY IF EXISTS "user_delete_own_try_on_jobs" ON public.try_on_jobs;

DROP POLICY IF EXISTS "user_select_own_try_on_results" ON public.try_on_results;
DROP POLICY IF EXISTS "user_insert_own_try_on_results" ON public.try_on_results;
DROP POLICY IF EXISTS "user_update_own_try_on_results" ON public.try_on_results;
DROP POLICY IF EXISTS "user_delete_own_try_on_results" ON public.try_on_results;

-- Policies for public.try_on_jobs
-- 1) Authenticated user can SELECT own jobs
CREATE POLICY "user_select_own_try_on_jobs"
    ON public.try_on_jobs FOR SELECT
    TO authenticated
    USING (user_id = auth.uid());

-- 2) Authenticated user can only insert own job with status PENDING
CREATE POLICY "user_insert_own_try_on_jobs"
    ON public.try_on_jobs FOR INSERT
    TO authenticated
    WITH CHECK (user_id = auth.uid() AND status = 'PENDING');

-- 3) Authenticated user can only update own in-flight job to CANCELLED (sensitive transitions are server-side)
CREATE POLICY "user_update_cancel_own_try_on_jobs"
    ON public.try_on_jobs FOR UPDATE
    TO authenticated
    USING (user_id = auth.uid() AND status IN ('PENDING', 'PROCESSING'))
    WITH CHECK (user_id = auth.uid() AND status = 'CANCELLED');

-- Policies for public.try_on_results
-- 1) Authenticated user can SELECT own results
CREATE POLICY "user_select_own_try_on_results"
    ON public.try_on_results FOR SELECT
    TO authenticated
    USING (user_id = auth.uid());

-- NOTE: No INSERT, UPDATE, or DELETE policies for authenticated role on public.try_on_results.
-- Only server-side generation (service_role) can insert results, preventing client-side tampering.

-- =============================================================================
-- 4. STORAGE BUCKET: tryon-results & RLS
-- =============================================================================
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES ('tryon-results', 'tryon-results', false, 10485760, ARRAY['image/jpeg', 'image/png', 'image/webp'])
ON CONFLICT (id) DO UPDATE SET
    public = false,
    file_size_limit = EXCLUDED.file_size_limit,
    allowed_mime_types = EXCLUDED.allowed_mime_types;

DROP POLICY IF EXISTS "user_select_own_tryon_results" ON storage.objects;
DROP POLICY IF EXISTS "user_insert_own_tryon_results" ON storage.objects;
DROP POLICY IF EXISTS "user_update_own_tryon_results" ON storage.objects;
DROP POLICY IF EXISTS "user_delete_own_tryon_results" ON storage.objects;

-- Authenticated user can read own results: tryon-results/{auth.uid()}/*
CREATE POLICY "user_select_own_tryon_results"
    ON storage.objects FOR SELECT
    TO authenticated
    USING (bucket_id = 'tryon-results' AND (storage.foldername(name))[1] = (select auth.uid()::text));

-- No direct client INSERT on tryon-results. Only server-side Edge Function writes results.

-- =============================================================================
-- 5. ATOMIC CANCELLATION RPC: cancel_try_on_job
-- =============================================================================
CREATE OR REPLACE FUNCTION public.cancel_try_on_job(
    p_job_id uuid DEFAULT NULL,
    p_request_id text DEFAULT NULL
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id uuid;
    v_job RECORD;
    v_credit_res jsonb;
    v_req_id text;
BEGIN
    -- Derive user identity securely from auth.uid()
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RETURN jsonb_build_object(
            'success', false,
            'error', 'unauthorized',
            'message', 'User must be authenticated'
        );
    END IF;

    IF p_job_id IS NULL AND (p_request_id IS NULL OR trim(p_request_id) = '') THEN
        RETURN jsonb_build_object(
            'success', false,
            'error', 'invalid_argument',
            'message', 'Either job_id or request_id must be provided'
        );
    END IF;

    -- Lock the job row
    SELECT * INTO v_job
    FROM public.try_on_jobs
    WHERE (p_job_id IS NOT NULL AND id = p_job_id)
       OR (p_request_id IS NOT NULL AND request_id = p_request_id)
    FOR UPDATE;

    IF NOT FOUND THEN
        -- If no job row exists yet but request_id was provided, release any held credit
        IF p_request_id IS NOT NULL THEN
            PERFORM public.release_credit(p_request_id);
        END IF;
        RETURN jsonb_build_object(
            'success', true,
            'status', 'CANCELLED',
            'message', 'No active job found; held credit released if any'
        );
    END IF;

    -- Check ownership
    IF v_job.user_id <> v_user_id THEN
        RETURN jsonb_build_object(
            'success', false,
            'error', 'ownership_mismatch',
            'message', 'Job belongs to another user'
        );
    END IF;

    -- If already completed, cannot cancel (prevents race condition: success vs cancellation)
    IF v_job.status = 'COMPLETED' THEN
        RETURN jsonb_build_object(
            'success', false,
            'error', 'already_completed',
            'job_id', v_job.id,
            'status', 'COMPLETED',
            'message', 'Cannot cancel already completed job'
        );
    END IF;

    -- If already cancelled, return idempotent success
    IF v_job.status = 'CANCELLED' THEN
        RETURN jsonb_build_object(
            'success', true,
            'job_id', v_job.id,
            'status', 'CANCELLED',
            'idempotent', true
        );
    END IF;

    -- Transition job to CANCELLED
    UPDATE public.try_on_jobs
    SET status = 'CANCELLED',
        completed_at = now()
    WHERE id = v_job.id;

    -- Release held credit
    v_req_id := COALESCE(p_request_id, v_job.request_id);
    IF v_req_id IS NOT NULL AND trim(v_req_id) <> '' THEN
        v_credit_res := public.release_credit(v_req_id);
    END IF;

    RETURN jsonb_build_object(
        'success', true,
        'job_id', v_job.id,
        'status', 'CANCELLED',
        'credit_released', true
    );
END;
$$;
