-- Migration: 20260915000004_credit_ledger_rls.sql
-- Description: Row Level Security (RLS) policies and least-privilege role permissions for the credit ledger.

-- 1. Enable Row Level Security (RLS) on both ledger tables
ALTER TABLE public.user_credit_balances ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.credit_transactions ENABLE ROW LEVEL SECURITY;

-- 2. Drop any pre-existing policies to ensure clean idempotent application
DROP POLICY IF EXISTS "user_select_own_balance" ON public.user_credit_balances;
DROP POLICY IF EXISTS "user_select_own_transactions" ON public.credit_transactions;

-- 3. SELECT policies: authenticated users can only view their own rows
CREATE POLICY "user_select_own_balance"
    ON public.user_credit_balances
    FOR SELECT
    TO authenticated
    USING ((select auth.uid()) = user_id);

CREATE POLICY "user_select_own_transactions"
    ON public.credit_transactions
    FOR SELECT
    TO authenticated
    USING ((select auth.uid()) = user_id);

-- 4. Revoke all direct mutation permissions from public/anon/authenticated roles.
-- Clients MUST NOT directly INSERT, UPDATE, or DELETE from credit ledger tables.
REVOKE ALL ON TABLE public.user_credit_balances FROM public, anon, authenticated;
REVOKE ALL ON TABLE public.credit_transactions FROM public, anon, authenticated;

-- Grant strictly SELECT to authenticated users
GRANT SELECT ON TABLE public.user_credit_balances TO authenticated;
GRANT SELECT ON TABLE public.credit_transactions TO authenticated;

-- 5. Revoke public/anon execute on credit ledger RPCs and grant exclusively to authenticated users
REVOKE EXECUTE ON FUNCTION public.hold_credit(text) FROM public, anon;
REVOKE EXECUTE ON FUNCTION public.consume_credit(text) FROM public, anon;
REVOKE EXECUTE ON FUNCTION public.release_credit(text) FROM public, anon;
REVOKE EXECUTE ON FUNCTION public.get_credit_balance() FROM public, anon;

GRANT EXECUTE ON FUNCTION public.hold_credit(text) TO authenticated;
GRANT EXECUTE ON FUNCTION public.consume_credit(text) TO authenticated;
GRANT EXECUTE ON FUNCTION public.release_credit(text) TO authenticated;
GRANT EXECUTE ON FUNCTION public.get_credit_balance() TO authenticated;
