-- Migration: 20260915000003_credit_ledger_rpcs.sql
-- Description: Server-authoritative PostgreSQL RPC functions for credit operations (hold, consume, release, balance).

-- =============================================================================
-- 1. RPC: hold_credit(p_operation_id text)
-- =============================================================================
CREATE OR REPLACE FUNCTION public.hold_credit(p_operation_id text)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id uuid;
    v_existing RECORD;
    v_free int;
    v_purchased int;
    v_held_free int;
    v_held_purchased int;
    v_available_free int;
    v_available_purchased int;
    v_total_available int;
    v_source text;
BEGIN
    -- 1. Derive user identity securely from auth.uid()
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RETURN jsonb_build_object(
            'success', false,
            'error', 'unauthorized',
            'message', 'User must be authenticated'
        );
    END IF;

    -- Validate operation_id argument
    IF p_operation_id IS NULL OR trim(p_operation_id) = '' THEN
        RETURN jsonb_build_object(
            'success', false,
            'error', 'invalid_argument',
            'message', 'operation_id cannot be null or empty'
        );
    END IF;

    -- 2. Check if transaction with this operation_id already exists (idempotency & ownership check)
    SELECT * INTO v_existing
    FROM public.credit_transactions
    WHERE operation_id = p_operation_id;

    IF FOUND THEN
        -- Ownership mismatch verification
        IF v_existing.user_id <> v_user_id THEN
            RETURN jsonb_build_object(
                'success', false,
                'error', 'ownership_mismatch',
                'message', 'Operation belongs to another user'
            );
        END IF;

        -- Idempotent return if already HELD
        IF v_existing.state = 'HELD' THEN
            -- Check if hold has expired
            IF v_existing.expires_at <= now() THEN
                RETURN jsonb_build_object(
                    'success', false,
                    'error', 'hold_expired',
                    'operation_id', p_operation_id,
                    'state', 'HELD'
                );
            END IF;

            RETURN jsonb_build_object(
                'success', true,
                'operation_id', p_operation_id,
                'state', 'HELD',
                'source', v_existing.source,
                'idempotent', true
            );
        END IF;

        -- Cannot re-hold finalized transaction
        RETURN jsonb_build_object(
            'success', false,
            'error', 'already_finalized',
            'state', v_existing.state,
            'message', 'Operation has already been ' || v_existing.state
        );
    END IF;

    -- 3. Lock user balance row to prevent race conditions & negative balance
    SELECT free_credits, purchased_credits
    INTO v_free, v_purchased
    FROM public.user_credit_balances
    WHERE user_id = v_user_id
    FOR UPDATE;

    -- Auto-provision if balance row does not exist yet
    IF NOT FOUND THEN
        INSERT INTO public.user_credit_balances (user_id, free_credits, purchased_credits, updated_at)
        VALUES (v_user_id, 2, 0, now())
        ON CONFLICT (user_id) DO UPDATE SET updated_at = now()
        RETURNING free_credits, purchased_credits INTO v_free, v_purchased;
    END IF;

    -- 4. Calculate active holds (non-expired HELD transactions)
    SELECT 
        COALESCE(SUM(CASE WHEN source = 'FREE' THEN amount ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN source = 'PURCHASED' THEN amount ELSE 0 END), 0)
    INTO v_held_free, v_held_purchased
    FROM public.credit_transactions
    WHERE user_id = v_user_id
      AND state = 'HELD'
      AND expires_at > now();

    v_available_free := v_free - v_held_free;
    v_available_purchased := v_purchased - v_held_purchased;
    v_total_available := v_available_free + v_available_purchased;

    -- 5. Prevent negative balance
    IF v_total_available < 1 THEN
        RETURN jsonb_build_object(
            'success', false,
            'error', 'insufficient_credits',
            'available_credits', GREATEST(0, v_total_available)
        );
    END IF;

    -- 6. Allocate source: consume FREE credits first, then PURCHASED
    IF v_available_free >= 1 THEN
        v_source := 'FREE';
    ELSE
        v_source := 'PURCHASED';
    END IF;

    -- 7. Insert HELD transaction with 5-minute expiry
    INSERT INTO public.credit_transactions (
        operation_id,
        user_id,
        amount,
        state,
        source,
        created_at,
        expires_at
    ) VALUES (
        p_operation_id,
        v_user_id,
        1,
        'HELD',
        v_source,
        now(),
        now() + interval '5 minutes'
    );

    RETURN jsonb_build_object(
        'success', true,
        'operation_id', p_operation_id,
        'state', 'HELD',
        'source', v_source,
        'available_credits', v_total_available - 1,
        'idempotent', false
    );
END;
$$;

-- =============================================================================
-- 2. RPC: consume_credit(p_operation_id text)
-- =============================================================================
CREATE OR REPLACE FUNCTION public.consume_credit(p_operation_id text)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id uuid;
    v_tx RECORD;
    v_balance RECORD;
BEGIN
    -- Derive user identity securely from auth.uid()
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RETURN jsonb_build_object('success', false, 'error', 'unauthorized');
    END IF;

    IF p_operation_id IS NULL OR trim(p_operation_id) = '' THEN
        RETURN jsonb_build_object('success', false, 'error', 'invalid_argument');
    END IF;

    -- Lock the transaction row
    SELECT * INTO v_tx
    FROM public.credit_transactions
    WHERE operation_id = p_operation_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'error', 'operation_not_found');
    END IF;

    -- Verify ownership
    IF v_tx.user_id <> v_user_id THEN
        RETURN jsonb_build_object('success', false, 'error', 'ownership_mismatch');
    END IF;

    -- Idempotency: if already CONSUMED, return success safely
    IF v_tx.state = 'CONSUMED' THEN
        RETURN jsonb_build_object(
            'success', true,
            'operation_id', p_operation_id,
            'state', 'CONSUMED',
            'source', v_tx.source,
            'idempotent', true,
            'message', 'already_consumed'
        );
    END IF;

    -- Cannot consume RELEASED
    IF v_tx.state = 'RELEASED' THEN
        RETURN jsonb_build_object(
            'success', false,
            'error', 'cannot_consume_released',
            'state', 'RELEASED'
        );
    END IF;

    -- Only HELD can become CONSUMED
    IF v_tx.state <> 'HELD' THEN
        RETURN jsonb_build_object(
            'success', false,
            'error', 'invalid_state',
            'state', v_tx.state
        );
    END IF;

    -- Lock the balance row to perform atomic deduction
    SELECT * INTO v_balance
    FROM public.user_credit_balances
    WHERE user_id = v_user_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'error', 'balance_not_found');
    END IF;

    -- Deduct from the appropriate source atomically
    IF v_tx.source = 'FREE' THEN
        IF v_balance.free_credits < v_tx.amount THEN
            RETURN jsonb_build_object('success', false, 'error', 'insufficient_free_balance');
        END IF;
        UPDATE public.user_credit_balances
        SET free_credits = free_credits - v_tx.amount,
            updated_at = now()
        WHERE user_id = v_user_id;
    ELSIF v_tx.source = 'PURCHASED' THEN
        IF v_balance.purchased_credits < v_tx.amount THEN
            RETURN jsonb_build_object('success', false, 'error', 'insufficient_purchased_balance');
        END IF;
        UPDATE public.user_credit_balances
        SET purchased_credits = purchased_credits - v_tx.amount,
            updated_at = now()
        WHERE user_id = v_user_id;
    ELSE
        RETURN jsonb_build_object('success', false, 'error', 'invalid_source');
    END IF;

    -- Mark transaction as CONSUMED
    UPDATE public.credit_transactions
    SET state = 'CONSUMED'
    WHERE operation_id = p_operation_id;

    RETURN jsonb_build_object(
        'success', true,
        'operation_id', p_operation_id,
        'state', 'CONSUMED',
        'source', v_tx.source,
        'idempotent', false
    );
END;
$$;

-- =============================================================================
-- 3. RPC: release_credit(p_operation_id text)
-- =============================================================================
CREATE OR REPLACE FUNCTION public.release_credit(p_operation_id text)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id uuid;
    v_tx RECORD;
BEGIN
    -- Derive user identity securely from auth.uid()
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RETURN jsonb_build_object('success', false, 'error', 'unauthorized');
    END IF;

    IF p_operation_id IS NULL OR trim(p_operation_id) = '' THEN
        RETURN jsonb_build_object('success', false, 'error', 'invalid_argument');
    END IF;

    -- Lock the transaction row
    SELECT * INTO v_tx
    FROM public.credit_transactions
    WHERE operation_id = p_operation_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'error', 'operation_not_found');
    END IF;

    -- Verify ownership
    IF v_tx.user_id <> v_user_id THEN
        RETURN jsonb_build_object('success', false, 'error', 'ownership_mismatch');
    END IF;

    -- Idempotency: if already RELEASED, return success safely
    IF v_tx.state = 'RELEASED' THEN
        RETURN jsonb_build_object(
            'success', true,
            'operation_id', p_operation_id,
            'state', 'RELEASED',
            'source', v_tx.source,
            'idempotent', true,
            'message', 'already_released'
        );
    END IF;

    -- Cannot release already CONSUMED operation
    IF v_tx.state = 'CONSUMED' THEN
        RETURN jsonb_build_object(
            'success', false,
            'error', 'cannot_release_consumed',
            'state', 'CONSUMED'
        );
    END IF;

    -- Only HELD can become RELEASED
    IF v_tx.state <> 'HELD' THEN
        RETURN jsonb_build_object(
            'success', false,
            'error', 'invalid_state',
            'state', v_tx.state
        );
    END IF;

    -- Transition state to RELEASED; balance remains completely UNCHANGED
    UPDATE public.credit_transactions
    SET state = 'RELEASED'
    WHERE operation_id = p_operation_id;

    RETURN jsonb_build_object(
        'success', true,
        'operation_id', p_operation_id,
        'state', 'RELEASED',
        'source', v_tx.source,
        'idempotent', false
    );
END;
$$;

-- =============================================================================
-- 4. RPC: get_credit_balance()
-- =============================================================================
CREATE OR REPLACE FUNCTION public.get_credit_balance()
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id uuid;
    v_free int;
    v_purchased int;
    v_held_free int;
    v_held_purchased int;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RETURN jsonb_build_object('success', false, 'error', 'unauthorized');
    END IF;

    SELECT free_credits, purchased_credits
    INTO v_free, v_purchased
    FROM public.user_credit_balances
    WHERE user_id = v_user_id;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
            'success', true,
            'free_credits', 0,
            'purchased_credits', 0,
            'active_holds', 0,
            'available_credits', 0
        );
    END IF;

    SELECT 
        COALESCE(SUM(CASE WHEN source = 'FREE' THEN amount ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN source = 'PURCHASED' THEN amount ELSE 0 END), 0)
    INTO v_held_free, v_held_purchased
    FROM public.credit_transactions
    WHERE user_id = v_user_id
      AND state = 'HELD'
      AND expires_at > now();

    RETURN jsonb_build_object(
        'success', true,
        'free_credits', v_free,
        'purchased_credits', v_purchased,
        'active_holds', (v_held_free + v_held_purchased),
        'available_credits', GREATEST(0, (v_free - v_held_free) + (v_purchased - v_held_purchased))
    );
END;
$$;
