import { AiTryOnProvider, TryOnProviderRequest, TryOnProviderResult } from "./types.ts";

/**
 * Controlled Test Try-On Provider for credit and job lifecycle verification.
 * Clearly separated from the real Gemini provider and explicitly marks results as test simulations.
 * Never consumes credits or reports real Try-On success.
 */
export class ApprovedTestTryOnProvider implements AiTryOnProvider {
  readonly name = "controlled_test_provider";

  isConfigured(): boolean {
    // Active for controlled testing of credit & job lifecycle
    return true;
  }

  async generate(request: TryOnProviderRequest): Promise<TryOnProviderResult> {
    if (!request.userPhotoBytes || request.userPhotoBytes.length === 0) {
      throw new Error("User photo data is required for test virtual try-on.");
    }

    // Controlled failure simulation
    if (request.requestId.includes("fail") || request.metadata?.simulateFailure === true) {
      throw new Error("Controlled test provider simulated failure for testing credit refund.");
    }

    // Returns clearly identifiable test simulation output bytes
    return {
      imageBytes: request.userPhotoBytes,
      mimeType: request.userPhotoMimeType || "image/jpeg",
      providerName: this.name,
      watermarkApplied: true,
      metadata: {
        mode: "phase2_credit_lifecycle_test",
        productId: request.productId,
        productBrand: request.product.brand
      }
    };
  }
}
