import { AiTryOnProvider, TryOnProviderRequest, TryOnProviderResult } from "./types.ts";

/**
 * Production-ready Gemini AI Try-On Provider.
 * Uses Google's image-capable Gemini model (gemini-3.1-flash-image) for high-fidelity virtual try-on.
 * Strictly executes server-side; API keys never enter client/Android code.
 */
export class GeminiTryOnProvider implements AiTryOnProvider {
  readonly name = "gemini";
  private readonly primaryModel = "gemini-3.1-flash-image";

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

    // 1. User photo (authoritative person reference)
    const userPhotoBase64 = this.uint8ArrayToBase64(request.userPhotoBytes);
    const userMimeType = request.userPhotoMimeType || "image/jpeg";

    // 2. Product garment image (authoritative garment reference)
    let productPhotoBase64: string | null = null;
    let productMimeType = "image/jpeg";
    const productImageUrl = request.product.primaryImageUrl || request.product.productImages?.[0];

    if (productImageUrl && productImageUrl.startsWith("http")) {
      try {
        const prodRes = await fetch(productImageUrl);
        if (prodRes.ok) {
          const prodBuf = await prodRes.arrayBuffer();
          productPhotoBase64 = this.uint8ArrayToBase64(new Uint8Array(prodBuf));
          productMimeType = prodRes.headers.get("content-type") || "image/jpeg";
        }
      } catch (err) {
        console.warn("Could not fetch product reference image, relying on text metadata:", err);
      }
    }

    // 3. Construct detailed try-on prompt
    const prompt = `Task: Realistic virtual fashion try-on.

Reference Images:
1. User Photo (Person Reference): Authoritative for the person's identity, facial features, skin tone, hair, body proportions, natural pose, and environment.
2. Garment Reference: Authoritative for the clothing item: ${request.product.name} by ${request.product.brand}${request.product.material ? `, Material: ${request.product.material}` : ""}${request.product.description ? `, Description: ${request.product.description}` : ""}.

Mandatory Instructions:
1. Generate a high-resolution photographic result of the SAME PERSON from the User Photo wearing the EXACT garment from the Garment Reference.
2. Preserve the person's exact face, identity, hair, expression, skin tone, body shape, and pose completely.
3. Replace/overlay ONLY the clothing on the person with the selected garment.
4. Faithfully preserve the garment's exact color, fabric texture, pattern, cut, neckline, sleeves, seams, drape, and silhouette.
5. Fit the garment naturally to the person's posture and body contour with realistic shadows, fabric creases, and ambient lighting matching the scene.
6. Do NOT redesign the garment. Do NOT invent new garment details. Do NOT add accessories, jewelry, or shoes. Do NOT change the background unless necessary for seamless composite blending.
7. Return an image output only.`;

    // 4. Build multimodal parts
    const parts: any[] = [
      { text: prompt },
      {
        inline_data: {
          mime_type: userMimeType,
          data: userPhotoBase64
        }
      }
    ];

    if (productPhotoBase64) {
      parts.push({
        inline_data: {
          mime_type: productMimeType,
          data: productPhotoBase64
        }
      });
    }

    const requestBody = {
      contents: [
        {
          parts: parts
        }
      ],
      generationConfig: {
        responseModalities: ["IMAGE"],
        imageConfig: {
          aspectRatio: "3:4",
          imageSize: "1K"
        }
      }
    };

    const url = `https://generativelanguage.googleapis.com/v1beta/models/${this.primaryModel}:generateContent?key=${apiKey}`;
    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(requestBody)
    });

    if (!response.ok) {
      const errText = await response.text();
      if (response.status === 429) {
        throw new Error(`Gemini API quota exhausted (429) on model ${this.primaryModel}: ${errText}`);
      }
      throw new Error(`Gemini API error (${response.status}) using model ${this.primaryModel}: ${errText}`);
    }

    const data = await response.json();
    const candidate = data.candidates?.[0];
    if (!candidate) {
      throw new Error(`Gemini (${this.primaryModel}) returned empty candidate response.`);
    }

    // Check for generated image in parts (supporting both camelCase and snake_case)
    const partWithImage = candidate.content?.parts?.find(
      (p: any) => p.inlineData || p.inline_data
    );

    if (partWithImage) {
      const imgData = partWithImage.inlineData || partWithImage.inline_data;
      const mimeType = imgData.mimeType || imgData.mime_type || "image/jpeg";
      const resBytes = this.base64ToUint8Array(imgData.data);

      return {
        imageBytes: resBytes,
        mimeType: mimeType,
        providerName: this.name,
        watermarkApplied: true,
        metadata: {
          model: this.primaryModel,
          finishReason: candidate.finishReason || "STOP"
        }
      };
    }

    throw new Error(`Gemini (${this.primaryModel}) response did not contain image data in candidates.`);
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

