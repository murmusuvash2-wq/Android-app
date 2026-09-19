-- Migration: 20260915000010_create_admins.sql
-- Description: Create admins table for RBAC and secure product_drafts RLS.

-- 1. Create admins table
CREATE TABLE IF NOT EXISTS public.admins (
    user_id uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.admins ENABLE ROW LEVEL SECURITY;

-- Admins can read their own admin status
DROP POLICY IF EXISTS "allow_read_own_admin" ON public.admins;
CREATE POLICY "allow_read_own_admin" ON public.admins
    FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

-- 2. Secure product_drafts RLS
DROP POLICY IF EXISTS "allow_admin_manage_drafts" ON public.product_drafts;
CREATE POLICY "allow_admin_manage_drafts" ON public.product_drafts
    FOR ALL
    TO authenticated
    USING (EXISTS (SELECT 1 FROM public.admins WHERE user_id = auth.uid()))
    WITH CHECK (EXISTS (SELECT 1 FROM public.admins WHERE user_id = auth.uid()));
