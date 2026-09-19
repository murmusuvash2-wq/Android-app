-- Migration: 20260915000009_create_product_drafts.sql
-- Description: Create product_drafts table for the Web Admin Product Pipeline.

CREATE TABLE IF NOT EXISTS public.product_drafts (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    source_url text NOT NULL,
    status text NOT NULL DEFAULT 'draft' CHECK (status IN ('draft', 'published', 'rejected')),
    extracted_data jsonb,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.product_drafts ENABLE ROW LEVEL SECURITY;

-- Allow authenticated admins to view and update drafts.
DROP POLICY IF EXISTS "allow_admin_manage_drafts" ON public.product_drafts;
CREATE POLICY "allow_admin_manage_drafts" ON public.product_drafts
    FOR ALL
    TO authenticated
    USING (true)
    WITH CHECK (true);
