-- Migration: 20260915000001_create_user_credit_balances.sql
-- Description: Create user_credit_balances table, non-negative constraints, and new user provisioning trigger.

-- 1. Create table user_credit_balances
CREATE TABLE IF NOT EXISTS public.user_credit_balances (
    user_id uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    free_credits integer NOT NULL DEFAULT 2,
    purchased_credits integer NOT NULL DEFAULT 0,
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT free_credits_non_negative CHECK (free_credits >= 0),
    CONSTRAINT purchased_credits_non_negative CHECK (purchased_credits >= 0)
);

-- Comments
COMMENT ON TABLE public.user_credit_balances IS 'Server-authoritative credit balances per authenticated user. Client direct mutations are prohibited.';
COMMENT ON COLUMN public.user_credit_balances.free_credits IS 'Remaining non-negative promotional or trial credits (default 2).';
COMMENT ON COLUMN public.user_credit_balances.purchased_credits IS 'Remaining non-negative paid credits.';

-- 2. Server-side mechanism for initial credit provisioning on auth.users creation
CREATE OR REPLACE FUNCTION public.handle_new_user_credits()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
BEGIN
    INSERT INTO public.user_credit_balances (user_id, free_credits, purchased_credits, updated_at)
    VALUES (NEW.id, 2, 0, now())
    ON CONFLICT (user_id) DO NOTHING;
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION public.handle_new_user_credits() IS 'Automatically and idempotently provisions 2 free credits when a new user registers in auth.users.';

-- 3. Trigger attached to auth.users
DROP TRIGGER IF EXISTS on_auth_user_created_credits ON auth.users;
CREATE TRIGGER on_auth_user_created_credits
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user_credits();
