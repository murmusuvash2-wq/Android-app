export interface ProductDetails {
  id: string;
  name: string;
  brand: string;
  price?: number;
  primaryImageUrl?: string;
  productImages?: string[];
  description?: string;
  material?: string;
}

export interface TryOnProviderRequest {
  productId: string;
  product: ProductDetails;
  userPhotoPath: string;
  userPhotoBytes?: Uint8Array;
  userPhotoMimeType?: string;
  userId: string;
  requestId: string;
  metadata?: Record<string, any>;
}

export interface TryOnProviderResult {
  imageBytes: Uint8Array;
  mimeType: string;
  providerName: string;
  watermarkApplied: boolean;
  metadata?: Record<string, string>;
}

export interface AiTryOnProvider {
  readonly name: string;
  isConfigured(): boolean;
  generate(request: TryOnProviderRequest): Promise<TryOnProviderResult>;
}
