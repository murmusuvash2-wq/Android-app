import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.4";
import { crypto } from "https://deno.land/std@0.177.0/crypto/mod.ts";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseClient = createClient(
      Deno.env.get("SUPABASE_URL") ?? "",
      Deno.env.get("SUPABASE_ANON_KEY") ?? "",
      { global: { headers: { Authorization: req.headers.get("Authorization")! } } }
    );

    const { data: { user }, error: authError } = await supabaseClient.auth.getUser();
    if (authError || !user) {
      return new Response(JSON.stringify({ error: "Unauthorized" }), { status: 401, headers: corsHeaders });
    }


    const supabaseAdmin = createClient(
      Deno.env.get("SUPABASE_URL") ?? "",
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? ""
    );

    // 1. RBAC Admin Check
    const { data: adminData, error: adminError } = await supabaseAdmin
      .from("admins")
      .select("user_id")
      .eq("user_id", user.id)
      .single();

    if (adminError || !adminData) {
      return new Response(JSON.stringify({ error: "Forbidden: Admin access required" }), { status: 403, headers: corsHeaders });
    }

    const { draft_id, updated_data } = await req.json();
    if (!draft_id || !updated_data) {
      return new Response(JSON.stringify({ error: "Missing draft_id or updated_data" }), { status: 400, headers: corsHeaders });
    }

    // 2. Validate product name and price
    if (!updated_data.name || typeof updated_data.name !== "string" || updated_data.name.trim().length === 0) {
      return new Response(JSON.stringify({ error: "Product name is required." }), { status: 400, headers: corsHeaders });
    }

    const price = Number(updated_data.price);
    if (isNaN(price) || price < 0) {
      return new Response(JSON.stringify({ error: `Invalid price: ${updated_data.price}. Must be a non-negative number.` }), { status: 400, headers: corsHeaders });
    }

    const originalPrice = updated_data.original_price !== undefined && updated_data.original_price !== null && updated_data.original_price !== ""
      ? Number(updated_data.original_price)
      : (updated_data.mrp !== undefined && updated_data.mrp !== null && updated_data.mrp !== "" ? Number(updated_data.mrp) : null);
    if (originalPrice !== null && (isNaN(originalPrice) || originalPrice < 0)) {
      return new Response(JSON.stringify({ error: `Invalid original price (MRP): ${originalPrice}. Must be a non-negative number.` }), { status: 400, headers: corsHeaders });
    }

    const rating = updated_data.rating !== undefined && updated_data.rating !== null && updated_data.rating !== ""
      ? Number(updated_data.rating)
      : null;
    if (rating !== null && (isNaN(rating) || rating < 0 || rating > 5)) {
      return new Response(JSON.stringify({ error: `Invalid rating: ${rating}. Must be between 0 and 5.` }), { status: 400, headers: corsHeaders });
    }

    const discountPercent = updated_data.discount_percent !== undefined && updated_data.discount_percent !== null && updated_data.discount_percent !== ""
      ? Number(updated_data.discount_percent)
      : (updated_data["discount%"] !== undefined && updated_data["discount%"] !== null && updated_data["discount%"] !== "" ? Number(updated_data["discount%"]) : null);
    if (discountPercent !== null && (isNaN(discountPercent) || discountPercent < 0 || discountPercent > 100)) {
      return new Response(JSON.stringify({ error: `Invalid discount percent: ${discountPercent}. Must be between 0 and 100.` }), { status: 400, headers: corsHeaders });
    }

    const reviewCount = updated_data.review_count !== undefined && updated_data.review_count !== null && updated_data.review_count !== ""
      ? parseInt(updated_data.review_count, 10)
      : (updated_data.rating_count !== undefined && updated_data.rating_count !== null && updated_data.rating_count !== "" ? parseInt(updated_data.rating_count, 10) : null);

    // Normalize and validate images
    const rawImages = updated_data.product_images || (updated_data.image_url ? [updated_data.image_url] : []);
    const images = Array.isArray(rawImages) ? rawImages.filter(Boolean) : [rawImages];
    for (const url of images) {
      if (typeof url !== "string" || (!url.startsWith("http://") && !url.startsWith("https://"))) {
        return new Response(JSON.stringify({ error: `Invalid image URL: ${url}. Must be absolute HTTP/HTTPS.` }), { status: 400, headers: corsHeaders });
      }
    }

    // Verify draft exists and is not already published
    const { data: draft, error: fetchError } = await supabaseAdmin
      .from("product_drafts")
      .select("status, source_url")
      .eq("id", draft_id)
      .single();

    if (fetchError || !draft) {
      throw new Error("Draft not found.");
    }
    if (draft.status === "published") {
      throw new Error("Draft is already published.");
    }

    // Normalize and validate merchant/product URL
    const merchantUrl = updated_data.merchant_url || updated_data.product_url || draft.source_url || null;
    if (merchantUrl && !merchantUrl.startsWith("http://") && !merchantUrl.startsWith("https://")) {
      return new Response(JSON.stringify({ error: `Invalid merchant/product URL: ${merchantUrl}. Must be absolute HTTP/HTTPS.` }), { status: 400, headers: corsHeaders });
    }

    // Normalize colors and sizes
    const rawColors = updated_data.colors || (updated_data.color ? [updated_data.color] : null);
    const colors = Array.isArray(rawColors) ? rawColors : (rawColors ? [rawColors] : null);
    const sizes = Array.isArray(updated_data.sizes) ? updated_data.sizes : (updated_data.sizes ? [updated_data.sizes] : null);

    // Generate unique ID for public.products (UUID)
    const newProductId = crypto.randomUUID();

    // Prepare product payload matching public.products schema
    const productPayload = {
      id: newProductId,
      name: updated_data.name.trim(),
      brand: (updated_data.brand || "Unknown").trim(),
      price: price,
      original_price: originalPrice,
      rating: rating,
      review_count: reviewCount,
      description: updated_data.description || null,
      material: updated_data.material || null,
      sizes: sizes,
      colors: colors,
      product_images: images,
      merchant_url: merchantUrl,
      external_product_id: updated_data.external_product_id || updated_data.product_id || null,
      site: updated_data.site || null,
      gender: updated_data.gender || null,
      category: updated_data.category || null,
      discount_percent: discountPercent,
      // Default engagement metrics
      love_count: 0,
      try_on_count: 0,
      sales_count: 0,
      trending_score: 0.0,
      is_active: true
    };

    // 1. Insert into public.products
    const { error: insertError } = await supabaseAdmin
      .from("products")
      .insert(productPayload);

    if (insertError) {
      throw new Error(`Failed to publish product: ${insertError.message}`);
    }

    // 2. Mark draft as published
    const { error: updateError } = await supabaseAdmin
      .from("product_drafts")
      .update({ 
        status: "published", 
        extracted_data: updated_data,
        updated_at: new Date().toISOString()
      })
      .eq("id", draft_id);

    if (updateError) {
      console.error("Warning: Failed to mark draft as published:", updateError);
    }

    return new Response(JSON.stringify({ success: true, product_id: newProductId }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
      status: 200,
    });

  } catch (error) {
    return new Response(JSON.stringify({ error: "Publish failed", message: error.message }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
      status: 500,
    });
  }
});
