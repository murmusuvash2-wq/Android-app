-- Migration: 20260918000011_extend_products_schema.sql
-- Description: Extend products table with catalog metadata attributes and index for deduplication

ALTER TABLE public.products
    ADD COLUMN IF NOT EXISTS external_product_id text,
    ADD COLUMN IF NOT EXISTS site text,
    ADD COLUMN IF NOT EXISTS gender text,
    ADD COLUMN IF NOT EXISTS category text,
    ADD COLUMN IF NOT EXISTS discount_percent numeric;

-- Composite index for rapid deduplication lookup by site and external_product_id
CREATE INDEX IF NOT EXISTS idx_products_site_external_id ON public.products (site, external_product_id);
