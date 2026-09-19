-- Migration: 20260915000006_create_storage_buckets_and_rls.sql
-- Description: Creates private storage buckets (profile-photos, tryon-photos) and RLS policies for storage.objects.

-- 1. Create private storage buckets for profile photos and try-on reference photos
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES
    ('profile-photos', 'profile-photos', false, 5242880, ARRAY['image/jpeg', 'image/png', 'image/webp']),
    ('tryon-photos', 'tryon-photos', false, 10485760, ARRAY['image/jpeg', 'image/png', 'image/webp'])
ON CONFLICT (id) DO UPDATE SET
    public = false,
    file_size_limit = EXCLUDED.file_size_limit,
    allowed_mime_types = EXCLUDED.allowed_mime_types;

-- 2. Drop any pre-existing policies for clean idempotency
DROP POLICY IF EXISTS "user_select_own_profile_photos" ON storage.objects;
DROP POLICY IF EXISTS "user_insert_own_profile_photos" ON storage.objects;
DROP POLICY IF EXISTS "user_update_own_profile_photos" ON storage.objects;
DROP POLICY IF EXISTS "user_delete_own_profile_photos" ON storage.objects;

DROP POLICY IF EXISTS "user_select_own_tryon_photos" ON storage.objects;
DROP POLICY IF EXISTS "user_insert_own_tryon_photos" ON storage.objects;
DROP POLICY IF EXISTS "user_update_own_tryon_photos" ON storage.objects;
DROP POLICY IF EXISTS "user_delete_own_tryon_photos" ON storage.objects;

-- 3. Profile photos RLS: scoped strictly to folder matching auth.uid()
CREATE POLICY "user_select_own_profile_photos"
    ON storage.objects FOR SELECT
    TO authenticated
    USING (bucket_id = 'profile-photos' AND (storage.foldername(name))[1] = (select auth.uid()::text));

CREATE POLICY "user_insert_own_profile_photos"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (bucket_id = 'profile-photos' AND (storage.foldername(name))[1] = (select auth.uid()::text));

CREATE POLICY "user_update_own_profile_photos"
    ON storage.objects FOR UPDATE
    TO authenticated
    USING (bucket_id = 'profile-photos' AND (storage.foldername(name))[1] = (select auth.uid()::text));

CREATE POLICY "user_delete_own_profile_photos"
    ON storage.objects FOR DELETE
    TO authenticated
    USING (bucket_id = 'profile-photos' AND (storage.foldername(name))[1] = (select auth.uid()::text));

-- 4. Try-on photos RLS: scoped strictly to folder matching auth.uid()
CREATE POLICY "user_select_own_tryon_photos"
    ON storage.objects FOR SELECT
    TO authenticated
    USING (bucket_id = 'tryon-photos' AND (storage.foldername(name))[1] = (select auth.uid()::text));

CREATE POLICY "user_insert_own_tryon_photos"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (bucket_id = 'tryon-photos' AND (storage.foldername(name))[1] = (select auth.uid()::text));

-- Allow replace/update of existing photo
CREATE POLICY "user_update_own_tryon_photos"
    ON storage.objects FOR UPDATE
    TO authenticated
    USING (bucket_id = 'tryon-photos' AND (storage.foldername(name))[1] = (select auth.uid()::text));

CREATE POLICY "user_delete_own_tryon_photos"
    ON storage.objects FOR DELETE
    TO authenticated
    USING (bucket_id = 'tryon-photos' AND (storage.foldername(name))[1] = (select auth.uid()::text));
