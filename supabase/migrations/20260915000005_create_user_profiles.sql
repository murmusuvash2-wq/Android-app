-- Migration: 20260915000005_create_user_profiles.sql
-- Description: User profiles table, auto-creation trigger on new auth users, and RLS policies.

-- 1. Create public.profiles table
CREATE TABLE IF NOT EXISTS public.profiles (
    id uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name text,
    phone text,
    top_size text,
    bottom_size text,
    shoe_size text,
    avatar_path text,
    updated_at timestamptz NOT NULL DEFAULT now()
);

-- 2. Create updated_at trigger for profiles
CREATE OR REPLACE FUNCTION public.set_profiles_updated_at()
RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_profiles_updated_at ON public.profiles;
CREATE TRIGGER trigger_profiles_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW
    EXECUTE FUNCTION public.set_profiles_updated_at();

-- 3. Trigger to auto-create a profile row when a new user signs up in auth.users
CREATE OR REPLACE FUNCTION public.handle_new_user_profile()
RETURNS trigger AS $$
BEGIN
    INSERT INTO public.profiles (id, updated_at)
    VALUES (NEW.id, now())
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created_profile ON auth.users;
CREATE TRIGGER on_auth_user_created_profile
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user_profile();

-- 4. Enable Row Level Security (RLS)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- 5. Drop any existing policies for idempotency
DROP POLICY IF EXISTS "user_select_own_profile" ON public.profiles;
DROP POLICY IF EXISTS "user_insert_own_profile" ON public.profiles;
DROP POLICY IF EXISTS "user_update_own_profile" ON public.profiles;

-- 6. Define RLS policies: authenticated users can only SELECT, INSERT, UPDATE their own profile
CREATE POLICY "user_select_own_profile"
    ON public.profiles
    FOR SELECT
    TO authenticated
    USING ((select auth.uid()) = id);

CREATE POLICY "user_insert_own_profile"
    ON public.profiles
    FOR INSERT
    TO authenticated
    WITH CHECK ((select auth.uid()) = id);

CREATE POLICY "user_update_own_profile"
    ON public.profiles
    FOR UPDATE
    TO authenticated
    USING ((select auth.uid()) = id)
    WITH CHECK ((select auth.uid()) = id);

-- 7. Revoke permissions from public/anon and grant strictly necessary permissions to authenticated
REVOKE ALL ON TABLE public.profiles FROM public, anon;
GRANT SELECT, INSERT, UPDATE ON TABLE public.profiles TO authenticated;
