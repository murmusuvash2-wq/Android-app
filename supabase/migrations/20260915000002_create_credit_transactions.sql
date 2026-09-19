-- Migration: 20260915000002_create_credit_transactions.sql
-- Description: Create credit_transactions ledger table, states, sources, and indexes.

-- 1. Create table credit_transactions
CREATE TABLE IF NOT EXISTS public.credit_transactions (
    operation_id text PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    amount integer NOT NULL DEFAULT 1,
    state text NOT NULL,
    source text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    expires_at timestamptz NOT NULL DEFAULT (now() + interval '5 minutes'),
    CONSTRAINT amount_positive CHECK (amount > 0),
    CONSTRAINT valid_transaction_state CHECK (state IN ('HELD', 'CONSUMED', 'RELEASED')),
    CONSTRAINT valid_transaction_source CHECK (source IN ('FREE', 'PURCHASED'))
);

-- Comments
COMMENT ON TABLE public.credit_transactions IS 'Immutable ledger tracking individual credit reservation holds, consumptions, and releases.';
COMMENT ON COLUMN public.credit_transactions.operation_id IS 'Unique client-provided idempotency key for the Try-On operation.';
COMMENT ON COLUMN public.credit_transactions.state IS 'Current lifecycle state: HELD, CONSUMED, or RELEASED.';
COMMENT ON COLUMN public.credit_transactions.source IS 'Balance bucket used: FREE or PURCHASED.';
COMMENT ON COLUMN public.credit_transactions.expires_at IS 'Timestamp after which an unconsumed HELD transaction is considered expired.';

-- 2. Performance indexes for user active hold lookups
CREATE INDEX IF NOT EXISTS idx_credit_transactions_user_state
    ON public.credit_transactions(user_id, state);

CREATE INDEX IF NOT EXISTS idx_credit_transactions_user_expires
    ON public.credit_transactions(user_id, expires_at)
    WHERE state = 'HELD';
