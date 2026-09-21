import { createClient } from "npm:@supabase/supabase-js@2";
import { AiProviderRouter, ProviderConfigurationError } from "./providers/router.ts";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, GET, OPTIONS",
  "Content-Type": "application/json"
};

// @ts-ignore
Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
  const anonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";

  const authHeader = req.headers.get("Authorization");
  if (!authHeader) {
    return new Response(
      JSON.stringify({ error: "unauthorized", message: "Missing Authorization header" }),
      { status: 401, headers: corsHeaders }
    );
  }

  // Create user-scoped client to derive auth identity securely
  const userClient = createClient(supabaseUrl, anonKey, {
    global: { headers: { Authorization: authHeader } }
  });

  // Flow A: Validate authentication
  const { data: { user }, error: authError } = await userClient.auth.getUser();
  if (authError || !user) {
    return new Response(
      JSON.stringify({ error: "unauthorized", message: "Invalid or expired session token" }),
      { status: 401, headers: corsHeaders }
    );
  }

  // Admin client for server-authoritative mutations and storage operations
  const adminClient = createClient(supabaseUrl, serviceRoleKey || anonKey);

  try {
    const body = await req.json();
    const action = body.action;

    // Handle cancellation action
    if (action === "cancel") {
      const requestId = body.requestId;
      const jobId = body.jobId;
      if (!requestId && !jobId) {
        return new Response(
          JSON.stringify({ error: "invalid_argument", message: "requestId or jobId required for cancellation" }),
          { status: 400, headers: corsHeaders }
        );
      }

      const { data: cancelRes, error: cancelError } = await userClient.rpc("cancel_try_on_job", {
        p_job_id: jobId || null,
        p_request_id: requestId || null
      });

      if (cancelError) {
        return new Response(
          JSON.stringify({ error: "cancellation_failed", message: cancelError.message }),
          { status: 500, headers: corsHeaders }
        );
      }

      return new Response(JSON.stringify(cancelRes), { status: 200, headers: corsHeaders });
    }

    const { productId, userPhotoPath, requestId } = body;

    if (!productId || !userPhotoPath || !requestId) {
      return new Response(
        JSON.stringify({
          error: "invalid_request",
          message: "Missing required fields: productId, userPhotoPath, requestId"
        }),
        { status: 400, headers: corsHeaders }
      );
    }

    // Flow B: Validate product exists and is active
    const { data: product, error: productError } = await adminClient
      .from("products")
      .select("id, name, brand, price, product_images, description, material, is_active")
      .eq("id", productId)
      .maybeSingle();

    if (productError || !product || product.is_active === false) {
      return new Response(
        JSON.stringify({ error: "product_unavailable", message: "Product is not available for virtual try-on" }),
        { status: 400, headers: corsHeaders }
      );
    }

    // Flow C: Validate user owns the photo path
    // Normalize path by stripping optional bucket prefix
    const normalizedPhotoPath = userPhotoPath.replace(/^tryon-photos\//, "");
    if (!normalizedPhotoPath.startsWith(`${user.id}/`)) {
      return new Response(
        JSON.stringify({ error: "invalid_photo", message: "User photo does not belong to the authenticated user" }),
        { status: 403, headers: corsHeaders }
      );
    }

    // Check photo exists in storage
    const { data: photoData, error: photoError } = await adminClient.storage
      .from("tryon-photos")
      .download(normalizedPhotoPath);

    if (photoError || !photoData) {
      return new Response(
        JSON.stringify({ error: "invalid_photo", message: "User reference photo not found in storage" }),
        { status: 400, headers: corsHeaders }
      );
    }

    // Flow 6: Idempotency check on existing job matching requestId
    const { data: existingJob } = await adminClient
      .from("try_on_jobs")
      .select("id, status, progress, error_code, error_message")
      .eq("user_id", user.id)
      .eq("request_id", requestId)
      .maybeSingle();

    if (existingJob) {
      if (existingJob.status === "COMPLETED") {
        // Return existing result idempotent without re-consuming credit
        const { data: existingResult } = await adminClient
          .from("try_on_results")
          .select("id, job_id, product_id, result_storage_path, watermark_applied, created_at")
          .eq("job_id", existingJob.id)
          .maybeSingle();

        const resultPath = existingResult?.result_storage_path || `${user.id}/${existingJob.id}.jpg`;
        const { data: signedUrlData } = await adminClient.storage
          .from("tryon-results")
          .createSignedUrl(resultPath, 3600);

        return new Response(
          JSON.stringify({
            jobId: existingJob.id,
            resultId: existingResult?.id,
            status: "COMPLETED",
            resultStoragePath: resultPath,
            signedResultUrl: signedUrlData?.signedUrl,
            watermarkApplied: existingResult?.watermark_applied ?? true,
            productId: productId,
            requestId: requestId,
            idempotent: true
          }),
          { status: 200, headers: corsHeaders }
        );
      }

      if (existingJob.status === "PROCESSING" || existingJob.status === "PENDING") {
        return new Response(
          JSON.stringify({
            jobId: existingJob.id,
            status: existingJob.status,
            progress: existingJob.progress,
            requestId: requestId,
            idempotent: true
          }),
          { status: 200, headers: corsHeaders }
        );
      }
    }

    // Flow D: Call hold_credit(requestId)
    const { data: holdRes, error: holdError } = await userClient.rpc("hold_credit", {
      p_operation_id: requestId
    });

    if (holdError || !holdRes?.success) {
      const errCode = holdRes?.error || "insufficient_credits";
      return new Response(
        JSON.stringify({
          error: errCode,
          message: holdRes?.message || "Insufficient credits for virtual try-on"
        }),
        { status: 402, headers: corsHeaders }
      );
    }

    let creditHeld = true;

    // Flow E: Create try_on_jobs row with PENDING
    const jobId = crypto.randomUUID();
    const { error: insertJobError } = await adminClient
      .from("try_on_jobs")
      .insert({
        id: jobId,
        user_id: user.id,
        product_id: productId,
        user_photo_storage_path: normalizedPhotoPath,
        status: "PENDING",
        progress: 10,
        request_id: requestId
      });

    if (insertJobError) {
      await userClient.rpc("release_credit", { p_operation_id: requestId });
      return new Response(
        JSON.stringify({ error: "job_creation_failed", message: insertJobError.message }),
        { status: 500, headers: corsHeaders }
      );
    }

    // Flow F & G: Update job status PROCESSING and invoke configured AI provider
    await adminClient
      .from("try_on_jobs")
      .update({ status: "PROCESSING", progress: 30 })
      .eq("id", jobId);

    try {
      const photoBuffer = await photoData.arrayBuffer();
      const photoBytes = new Uint8Array(photoBuffer);

      const router = new AiProviderRouter();
      const providerResult = await router.executeTryOn({
        productId: product.id,
        product: {
          id: product.id,
          name: product.name,
          brand: product.brand,
          price: product.price,
          primaryImageUrl: Array.isArray(product.product_images) && product.product_images.length > 0 ? product.product_images[0] : "",
          productImages: product.product_images,
          description: product.description,
          material: product.material
        },
        userPhotoPath: normalizedPhotoPath,
        userPhotoBytes: photoBytes,
        userPhotoMimeType: photoData.type || "image/jpeg",
        userId: user.id,
        requestId: requestId,
        metadata: {
          simulateFailure: body.simulateFailure === true || requestId.includes("fail")
        }
      });

      // Flow H: Store result in private tryon-results bucket
      const resultStoragePath = `${user.id}/${jobId}.jpg`;
      const { error: uploadError } = await adminClient.storage
        .from("tryon-results")
        .upload(resultStoragePath, providerResult.imageBytes, {
          contentType: providerResult.mimeType || "image/jpeg",
          upsert: true
        });

      if (uploadError) {
        throw new Error(`Failed to store try-on result: ${uploadError.message}`);
      }

      // Flow I: Create try_on_results row
      const resultId = crypto.randomUUID();
      const { error: insertResultError } = await adminClient
        .from("try_on_results")
        .insert({
          id: resultId,
          job_id: jobId,
          user_id: user.id,
          product_id: product.id,
          result_storage_path: resultStoragePath,
          watermark_applied: providerResult.watermarkApplied
        });

      if (insertResultError) {
        throw new Error(`Failed to record try-on result: ${insertResultError.message}`);
      }

      // Flow J: Call consume_credit(requestId) ONLY after successful result generation
      const { data: consumeRes, error: consumeError } = await userClient.rpc("consume_credit", {
        p_operation_id: requestId
      });

      if (consumeError || !consumeRes?.success) {
        console.error("Warning: Credit consumption RPC encountered issue:", consumeError || consumeRes);
      } else {
        creditHeld = false;
      }

      // Flow K: Mark job COMPLETED
      await adminClient
        .from("try_on_jobs")
        .update({
          status: "COMPLETED",
          progress: 100,
          provider: providerResult.providerName,
          completed_at: new Date().toISOString()
        })
        .eq("id", jobId);

      // Create signed URL for authenticated user access
      const { data: signedUrlData } = await adminClient.storage
        .from("tryon-results")
        .createSignedUrl(resultStoragePath, 3600);

      // Flow L: Return job and result metadata
      return new Response(
        JSON.stringify({
          jobId: jobId,
          resultId: resultId,
          status: "COMPLETED",
          resultStoragePath: resultStoragePath,
          signedResultUrl: signedUrlData?.signedUrl,
          watermarkApplied: providerResult.watermarkApplied,
          productId: product.id,
          requestId: requestId,
          completedAt: new Date().toISOString()
        }),
        { status: 200, headers: corsHeaders }
      );

    } catch (genError: any) {
      // Flow on failure: release held credit, NEVER consume on failure
      if (creditHeld) {
        try {
          await userClient.rpc("release_credit", { p_operation_id: requestId });
        } catch (releaseErr) {
          console.error("Failed to release held credit:", releaseErr);
        }
      }

      const isConfigError = genError instanceof ProviderConfigurationError;
      const errorCode = isConfigError ? "PROVIDER_NOT_CONFIGURED" : "GENERATION_FAILED";
      const errorMessage = genError.message || "Virtual try-on generation failed.";

      await adminClient
        .from("try_on_jobs")
        .update({
          status: "FAILED",
          error_code: errorCode,
          error_message: errorMessage,
          completed_at: new Date().toISOString()
        })
        .eq("id", jobId);

      return new Response(
        JSON.stringify({
          error: errorCode,
          message: errorMessage,
          jobId: jobId,
          requestId: requestId
        }),
        { status: isConfigError ? 503 : 500, headers: corsHeaders }
      );
    }

  } catch (err: any) {
    return new Response(
      JSON.stringify({ error: "server_error", message: err.message || "Unexpected internal error" }),
      { status: 500, headers: corsHeaders }
    );
  }
});
