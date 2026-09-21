import { AiTryOnProvider, TryOnProviderRequest, TryOnProviderResult } from "./types.ts";
import { GeminiTryOnProvider } from "./gemini.ts";
import { ApprovedTestTryOnProvider } from "./test_provider.ts";

export class ProviderConfigurationError extends Error {
  constructor(message: string = "No AI try-on provider is configured with valid credentials in backend secrets.") {
    super(message);
    this.name = "ProviderConfigurationError";
  }
}

/**
 * AI Provider Router that chains providers: Provider A -> Provider B -> Provider C.
 * Selects the first available provider with configured credentials.
 * If none are configured, raises a clear ProviderConfigurationError without faking results.
 */
export class AiProviderRouter {
  private providers: AiTryOnProvider[];

  constructor(customProviders?: AiTryOnProvider[]) {
    this.providers = customProviders || [
      new ApprovedTestTryOnProvider(),
      new GeminiTryOnProvider()
    ];
  }

  /**
   * Returns the first configured provider or null if none is configured.
   */
  getActiveProvider(): AiTryOnProvider | null {
    for (const provider of this.providers) {
      if (provider.isConfigured()) {
        return provider;
      }
    }
    return null;
  }

  /**
   * Executes try-on generation using the first configured provider.
   * If a provider fails with a transient or recoverable error, the router can fallback to subsequent providers.
   */
  async executeTryOn(request: TryOnProviderRequest): Promise<TryOnProviderResult> {
    const configuredProviders = this.providers.filter(p => p.isConfigured());

    if (configuredProviders.length === 0) {
      throw new ProviderConfigurationError(
        "No AI try-on provider has valid credentials configured in Supabase secrets (checked Gemini and approved test providers)."
      );
    }

    let lastError: Error | null = null;
    for (const provider of configuredProviders) {
      try {
        const result = await provider.generate(request);
        return result;
      } catch (err: any) {
        lastError = err instanceof Error ? err : new Error(String(err));
        console.warn(`Provider ${provider.name} failed: ${lastError.message}. Attempting next provider in chain.`);
      }
    }

    throw lastError || new Error("All configured AI try-on providers failed.");
  }
}
