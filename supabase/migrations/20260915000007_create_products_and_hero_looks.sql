-- Migration: 20260915000007_create_products_and_hero_looks.sql
-- Description: Products catalog table, hero looks table, normalized merchants, RLS policies, and catalog seed data.

-- 1. Create public.products table
CREATE TABLE IF NOT EXISTS public.products (
    id text PRIMARY KEY,
    name text NOT NULL,
    brand text NOT NULL,
    product_images text[] NOT NULL,
    price numeric NOT NULL CHECK (price >= 0),
    original_price numeric CHECK (original_price IS NULL OR original_price >= 0),
    rating numeric CHECK (rating IS NULL OR (rating >= 0 AND rating <= 5)),
    review_count integer CHECK (review_count IS NULL OR review_count >= 0),
    description text,
    style_tip text,
    sizes text[],
    colors text[],
    material text,
    merchant_id text,
    merchant_url text,
    created_at timestamptz NOT NULL DEFAULT now(),
    love_count integer NOT NULL DEFAULT 0 CHECK (love_count >= 0),
    try_on_count integer NOT NULL DEFAULT 0 CHECK (try_on_count >= 0),
    sales_count integer NOT NULL DEFAULT 0 CHECK (sales_count >= 0),
    trending_score numeric NOT NULL DEFAULT 0 CHECK (trending_score >= 0),
    is_active boolean NOT NULL DEFAULT true
);

-- 2. Products Row-Level Security
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "allow_read_active_products" ON public.products;
CREATE POLICY "allow_read_active_products" ON public.products
    FOR SELECT
    TO authenticated, anon
    USING (is_active = true);

-- 3. Normalized product_merchants table for multi-merchant price tracking
CREATE TABLE IF NOT EXISTS public.product_merchants (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id text NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    merchant_name text NOT NULL,
    product_url text NOT NULL,
    price numeric NOT NULL CHECK (price >= 0),
    in_stock boolean NOT NULL DEFAULT true,
    last_checked_at timestamptz DEFAULT now(),
    is_active boolean NOT NULL DEFAULT true
);

ALTER TABLE public.product_merchants ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "allow_read_active_product_merchants" ON public.product_merchants;
CREATE POLICY "allow_read_active_product_merchants" ON public.product_merchants
    FOR SELECT
    TO authenticated, anon
    USING (is_active = true);

-- 4. Create public.hero_looks table
CREATE TABLE IF NOT EXISTS public.hero_looks (
    id text PRIMARY KEY,
    product_id text NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    hanger_image text,
    worn_image text,
    sort_order integer NOT NULL DEFAULT 0,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.hero_looks ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "allow_read_active_hero_looks" ON public.hero_looks;
CREATE POLICY "allow_read_active_hero_looks" ON public.hero_looks
    FOR SELECT
    TO authenticated, anon
    USING (is_active = true);

-- 5. Seed Catalog Products (Canonical Mock Catalog - 12 Products)
INSERT INTO public.products (
    id, name, brand, product_images, price, original_price, rating, review_count,
    description, style_tip, sizes, colors, material, merchant_id, merchant_url,
    love_count, try_on_count, sales_count, trending_score, is_active
) VALUES
(
    '1',
    'Oversized Cashmere Trench',
    'ZARA',
    ARRAY[
        'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=800&q=80'
    ],
    12999.0,
    14999.0,
    4.8,
    124,
    'Spun from ultra-soft Mongolian cashmere with an elegant draped storm flap and tonal horn buttons.',
    'Layer open over high-waisted wool trousers and pointed ankle boots for a structured silhouette.',
    ARRAY['XS', 'S', 'M', 'L', 'XL'],
    ARRAY['Camel', 'Oatmeal', 'Black'],
    '100% Mongolian Cashmere',
    'zara',
    'https://www.zara.com/in/en/oversized-cashmere-trench-p01.html',
    420,
    890,
    340,
    98.5,
    true
),
(
    '2',
    'Tailored Wool Overcoat',
    'H&M',
    ARRAY[
        'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?auto=format&fit=crop&w=800&q=80'
    ],
    8999.0,
    10999.0,
    4.6,
    89,
    'Structured double-breasted silhouette cut from premium recycled wool blend.',
    'Pair with an oatmeal rollneck and leather loafers for timeless winter sophistication.',
    ARRAY['S', 'M', 'L', 'XL'],
    ARRAY['Charcoal', 'Navy'],
    '70% Recycled Wool, 30% Polyamide',
    'hm',
    'https://www2.hm.com/en_in/productpage.02.html',
    310,
    620,
    210,
    91.0,
    true
),
(
    '3',
    'Minimalist Linen Blazer',
    'MANGO',
    ARRAY[
        'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1509631179647-0177331693ae?auto=format&fit=crop&w=800&q=80'
    ],
    6590.0,
    7990.0,
    4.9,
    210,
    'Breathable pure European linen tailored with relaxed notch lapels and natural corozo buttons.',
    'Wear cuffs slightly pushed up with matching wide-leg trousers and gold hoops.',
    ARRAY['XS', 'S', 'M', 'L'],
    ARRAY['Beige', 'White', 'Sage'],
    '100% European Linen',
    'mango',
    'https://shop.mango.com/in/women/jackets-blazers/minimalist-linen-blazer_03.html',
    580,
    1120,
    450,
    95.2,
    true
),
(
    '4',
    'Structured Oxford & Trousers',
    'ZARA',
    ARRAY[
        'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80'
    ],
    4590.0,
    null,
    4.5,
    67,
    'Classic crisp cotton Oxford pairing seamlessly with straight-leg pleats.',
    'Half-tuck into belted charcoal trousers for an effortlessly sharp weekday profile.',
    ARRAY['S', 'M', 'L', 'XL'],
    ARRAY['Light Blue', 'White'],
    '100% Poplin Cotton',
    'zara',
    'https://www.zara.com/in/en/structured-oxford-p04.html',
    195,
    430,
    180,
    88.0,
    true
),
(
    '5',
    'Emerald Satin Maxi Dress',
    'URBANIC',
    ARRAY[
        'https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80'
    ],
    3790.0,
    4590.0,
    4.7,
    148,
    'Fluid bias-cut lustrous satin dress with an open cowl back and subtle train.',
    'Style minimally with delicate barely-there metallic sandals and a sleek low chignon.',
    ARRAY['XS', 'S', 'M'],
    ARRAY['Emerald Green', 'Champagne'],
    '95% Silk Satin, 5% Elastane',
    'urbanic',
    'https://www.urbanic.com/in/details/emerald-satin-maxi-dress-05',
    610,
    1350,
    390,
    96.0,
    true
),
(
    '6',
    'Pastel Co-ord Loungewear',
    'ASOS',
    ARRAY[
        'https://images.unsplash.com/photo-1509631179647-0177331693ae?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80'
    ],
    3290.0,
    null,
    null,
    null,
    'Soft waffle-knit matching ensemble designed for effortless elevated lounging.',
    'Ideal for off-duty days; finish the look with chunky slides and a slouchy tote.',
    ARRAY['S', 'M', 'L'],
    ARRAY['Lilac', 'Mint', 'Butter'],
    '80% Organic Cotton, 20% Recycled Poly',
    'asos',
    'https://www.asos.com/in/pastel-coord-loungewear-06',
    140,
    290,
    120,
    82.5,
    true
),
(
    '7',
    'Pleated Chiffon Midi Dress',
    'MANGO',
    ARRAY[
        'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=800&q=80'
    ],
    5590.0,
    6990.0,
    4.7,
    92,
    'Delicate accordion pleats through a tiered midi silhouette with poet sleeves.',
    'Style with slingback block heels and a woven shoulder bag.',
    ARRAY['XS', 'S', 'M', 'L'],
    ARRAY['Sage Green', 'Dusty Rose'],
    '100% Recycled Polyester Chiffon',
    'mango',
    'https://shop.mango.com/in/women/dresses/pleated-midi-dress_07.html',
    260,
    540,
    175,
    90.5,
    true
),
(
    '8',
    'Cropped Bouclé Jacket',
    'ZARA',
    ARRAY[
        'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1509631179647-0177331693ae?auto=format&fit=crop&w=800&q=80'
    ],
    6990.0,
    8490.0,
    4.8,
    115,
    'Boxy cropped jacket in textured wool-blend bouclé featuring jewel buttons and patch pockets.',
    'Layer over a fine-knit turtleneck with tailored high-rise denim.',
    ARRAY['XS', 'S', 'M', 'L'],
    ARRAY['Ivory', 'Navy'],
    '65% Wool, 35% Cotton Bouclé',
    'zara',
    'https://www.zara.com/in/en/cropped-boucle-jacket-p08.html',
    380,
    790,
    230,
    93.0,
    true
),
(
    'home_trending_1',
    'Linen Day Dress',
    'URBANIC',
    ARRAY[
        'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=400&q=80'
    ],
    2499.0,
    null,
    4.6,
    76,
    'Effortless A-line linen dress with subtle tiering and adjustable straps.',
    'Pair with woven espadrilles and a straw bag.',
    ARRAY['XS', 'S', 'M', 'L'],
    ARRAY['Terracotta', 'Off-White'],
    '100% Linen',
    'urbanic',
    'https://www.urbanic.com/in/details/linen-day-dress-ht1',
    490,
    980,
    310,
    94.0,
    true
),
(
    'home_trending_2',
    'Knit Polo & Chinos',
    'ZARA',
    ARRAY[
        'https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?auto=format&fit=crop&w=400&q=80'
    ],
    3999.0,
    null,
    4.5,
    58,
    'Fine-gauge cotton knit polo paired with slim tapered stretch chinos.',
    'Team with white minimal trainers and a classic leather watch.',
    ARRAY['M', 'L', 'XL'],
    ARRAY['Sand', 'Navy'],
    '100% Mercerized Cotton',
    'zara',
    'https://www.zara.com/in/en/knit-polo-chinos-ht2.html',
    230,
    510,
    195,
    89.5,
    true
),
(
    'home_trending_3',
    'Everyday Denim & Tee',
    'LEVI''S',
    ARRAY[
        'https://images.unsplash.com/photo-1558769132-cb1aea458c5e?auto=format&fit=crop&w=400&q=80'
    ],
    2899.0,
    null,
    4.7,
    110,
    'Iconic straight-fit mid-wash denim with heavyweight combed cotton graphic tee.',
    'Roll cuffs once over high-top canvas sneakers.',
    ARRAY['28', '30', '32', '34'],
    ARRAY['Medium Indigo', 'Faded Black'],
    '100% Cotton Denim',
    'levis',
    'https://www.levi.in/everyday-denim-tee-ht3',
    370,
    720,
    280,
    92.0,
    true
),
(
    'home_trending_4',
    'Blush Co-ord Set',
    'H&M',
    ARRAY[
        'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=400&q=80'
    ],
    3299.0,
    null,
    4.6,
    84,
    'Matching pastel cropped blouse and fluid midi skirt in soft drape rayon.',
    'Pair with nude block heels and sculptural silver earrings.',
    ARRAY['XS', 'S', 'M'],
    ARRAY['Blush Pink', 'Dusty Rose'],
    '100% Viscose Rayon',
    'hm',
    'https://www2.hm.com/en_in/productpage.ht4.html',
    340,
    680,
    220,
    90.0,
    true
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    brand = EXCLUDED.brand,
    product_images = EXCLUDED.product_images,
    price = EXCLUDED.price,
    original_price = EXCLUDED.original_price,
    rating = EXCLUDED.rating,
    review_count = EXCLUDED.review_count,
    description = EXCLUDED.description,
    style_tip = EXCLUDED.style_tip,
    sizes = EXCLUDED.sizes,
    colors = EXCLUDED.colors,
    material = EXCLUDED.material,
    merchant_id = EXCLUDED.merchant_id,
    merchant_url = EXCLUDED.merchant_url,
    love_count = EXCLUDED.love_count,
    try_on_count = EXCLUDED.try_on_count,
    sales_count = EXCLUDED.sales_count,
    trending_score = EXCLUDED.trending_score,
    is_active = EXCLUDED.is_active;

-- 6. Seed Product Merchants
INSERT INTO public.product_merchants (product_id, merchant_name, product_url, price, in_stock, is_active)
VALUES
('1', 'ZARA', 'https://www.zara.com/in/en/oversized-cashmere-trench-p01.html', 12999.0, true, true),
('2', 'H&M', 'https://www2.hm.com/en_in/productpage.02.html', 8999.0, true, true),
('3', 'MANGO', 'https://shop.mango.com/in/women/jackets-blazers/minimalist-linen-blazer_03.html', 6590.0, true, true),
('4', 'ZARA', 'https://www.zara.com/in/en/structured-oxford-p04.html', 4590.0, true, true),
('5', 'URBANIC', 'https://www.urbanic.com/in/details/emerald-satin-maxi-dress-05', 3790.0, true, true),
('6', 'ASOS', 'https://www.asos.com/in/pastel-coord-loungewear-06', 3290.0, true, true),
('7', 'MANGO', 'https://shop.mango.com/in/women/dresses/pleated-midi-dress_07.html', 5590.0, true, true),
('8', 'ZARA', 'https://www.zara.com/in/en/cropped-boucle-jacket-p08.html', 6990.0, true, true),
('home_trending_1', 'URBANIC', 'https://www.urbanic.com/in/details/linen-day-dress-ht1', 2499.0, true, true),
('home_trending_2', 'ZARA', 'https://www.zara.com/in/en/knit-polo-chinos-ht2.html', 3999.0, true, true),
('home_trending_3', 'LEVI''S', 'https://www.levi.in/everyday-denim-tee-ht3', 2899.0, true, true),
('home_trending_4', 'H&M', 'https://www2.hm.com/en_in/productpage.ht4.html', 3299.0, true, true)
ON CONFLICT DO NOTHING;

-- 7. Seed Hero Looks (3 curated showcase pairs matching canonical products)
INSERT INTO public.hero_looks (id, product_id, hanger_image, worn_image, sort_order, is_active)
VALUES
(
    'hero_1',
    '1',
    'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80',
    1,
    true
),
(
    'hero_2',
    '3',
    'https://images.unsplash.com/photo-1509631179647-0177331693ae?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80',
    2,
    true
),
(
    'hero_3',
    '4',
    'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80',
    3,
    true
)
ON CONFLICT (id) DO UPDATE SET
    product_id = EXCLUDED.product_id,
    hanger_image = EXCLUDED.hanger_image,
    worn_image = EXCLUDED.worn_image,
    sort_order = EXCLUDED.sort_order,
    is_active = EXCLUDED.is_active;
