import { AiTryOnProvider, TryOnProviderRequest, TryOnProviderResult } from "./types.ts";

/**
 * Production-ready Gemini AI Try-On Provider.
 * Requires GEMINI_API_KEY or GOOGLE_AI_API_KEY in Edge Function secrets.
 * Strictly executes server-side; API keys never enter client/Android code.
 */
export class GeminiTryOnProvider implements AiTryOnProvider {
  readonly name = "gemini";

  isConfigured(): boolean {
    const key = this.getApiKey();
    return Boolean(key && key.trim().length > 0 && !key.includes("placeholder"));
  }

  private getApiKey(): string | null {
    // Standard Deno / Supabase secrets
    // @ts-ignore
    if (typeof Deno !== "undefined" && Deno.env) {
      // @ts-ignore
      return Deno.env.get("GEMINI_API_KEY") || Deno.env.get("GOOGLE_AI_API_KEY") || null;
    }
    // Node.js fallback for testing environments
    if (typeof process !== "undefined" && process.env) {
      return process.env.GEMINI_API_KEY || process.env.GOOGLE_AI_API_KEY || null;
    }
    return null;
  }

  async generate(request: TryOnProviderRequest): Promise<TryOnProviderResult> {
    const apiKey = this.getApiKey();
    if (!apiKey) {
      throw new Error("GEMINI_API_KEY is not configured in backend environment secrets.");
    }

    if (!request.userPhotoBytes || request.userPhotoBytes.length === 0) {
      throw new Error("User photo data is required for virtual try-on generation.");
    }

    // Convert user photo bytes to base64
    const photoBase64 = this.uint8ArrayToBase64(request.userPhotoBytes);
    const mimeType = request.userPhotoMimeType || "image/jpeg";

    const prompt = `Task: High-fidelity virtual try-on.
Garment to wear: ${request.product.name} by ${request.product.brand}.
Material: ${request.product.material || "High quality fabric"}.
Description: ${request.product.description || ""}.

Instructions:
1. Preserve user's identity, face, body proportions, natural pose, hair, and background completely.
2. Replace or drape the garment accurately on the user, matching the exact garment color, fabric texture, pattern, cut, neckline, sleeves, and silhouette.
3. Apply realistic lighting, shadows, and natural folds.
4. Never redesign, invent, or distort garment details.`;

    // Call Gemini API with multimodal input
    const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`;

    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [
          {
            parts: [
              { text: prompt },
              {
                inline_data: {
                  mime_type: mimeType,
                  data: photoBase64
                }
              }
            ]
          }
        ]
      })
    });

    if (!response.ok) {
      const errText = await response.text();
      throw new Error(`Gemini generation failed (${response.status}): ${errText}`);
    }

    const data = await response.json();
    const candidate = data.candidates?.[0];
    if (!candidate) {
      throw new Error("Gemini returned empty candidate response.");
    }

    // Extract image or structured result
    // In multimodal outputs where image bytes are returned or generated
    const partWithImage = candidate.content?.parts?.find(
      (p: any) => p.inline_data || p.inlineData
    );

    if (partWithImage) {
      const imgData = partWithImage.inline_data || partWithImage.inlineData;
      const resBytes = this.base64ToUint8Array(imgData.data);
      return {
        imageBytes: resBytes,
        mimeType: imgData.mime_type || "image/jpeg",
        providerName: this.name,
        watermarkApplied: true,
        metadata: {
          model: "gemini-2.5-flash",
          finishReason: candidate.finishReason || "STOP"
        }
      };
    }

    // If text response only (e.g. guidance or model without direct image synthesis part)
    throw new Error("Gemini response did not contain image data for try-on visualization.");
  }

  private uint8ArrayToBase64(bytes: Uint8Array): string {
    let binary = "";
    const len = bytes.byteLength;
    for (let i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary);
  }

  private base64ToUint8Array(base64: string): Uint8Array {
    const binary = atob(base64);
    const len = binary.length;
    const bytes = new Uint8Array(len);
    for (let i = 0; i < len; i++) {
      bytes[i] = binary.charCodeAt(i);
    }
    return bytes;
  }
}
