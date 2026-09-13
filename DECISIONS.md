# Locked Product Decisions

**Brand Tagline:** "Try. Love. Buy."

This document records the established product decisions, architectural constraints, and functional boundaries for the application. All future modifications must adhere strictly to these locked decisions.

---

## 1. Primary Navigation

The application uses a 4-tab bottom navigation hierarchy:

```
Home  |  Looks  |  Discover  |  Me
```

- No additional top-level navigation tabs, side drawers, or floating menus may be introduced.

---

## 2. Looks Nomenclature & Sections

The second tab must strictly be named **"Looks"**.

### Approved Sections
1. **Recent**
2. **Favourites**
3. **Price Tracking**

### Explicitly Prohibited Terms & Sections
The following concepts must **NOT** be created or added anywhere in the product:
- ❌ Closet
- ❌ Wardrobe
- ❌ Digital Wardrobe
- ❌ Collections
- ❌ Boards

---

## 3. Result Screen Hierarchy & Experience

The Result Screen represents the culmination of the try-on experience and follows this strict hierarchy:

1. **Header:** Title ("Your New Look") with back navigation.
2. **Hero Presentation:** The completed **AI-generated result image** is the sole primary hero image in the main viewport.
   - **No Default Before/After:** Do **not** display a Before/After split screen or segmented toggle by default.
   - The user's input photo remains available in memory for processing logic, but is not displayed alongside the hero result.
3. **Product Information:**
   - Brand name
   - Product title
   - Formatted price
4. **Ratings / Reviews:** Conditional only (see Section 5 below).
5. **Compact Actions Row:**
   - `Save`
   - `Download`
   - `Share`
   - `🔔 Price Drop` (compact button)
6. **Primary Purchase CTA:** `Buy ↗` (prominent dark CTA).
7. **Secondary Actions:**
   - `Remove Watermark · Watch Ad`
   - `Try Another`

---

## 4. Price Drop Behavior

- Must remain a **compact action button** inside the action row (not an oversized card).
- **Default State:** `🔔 Price Drop`
- **Active State:** `🔔 Price Drop ✓`
- **Current Behavior:** Updates local/mock state and displays immediate toast feedback.
- **Future Backend Behavior:** Registers the product ID against the authenticated user account for background price scraping and push notifications on price drops.

---

## 5. Ratings & Merchant Reviews

- Supported data model fields:
  - `rating: Double?`
  - `reviewCount: Int?`
  - `ratingSource: String?`
- **Conditional Visibility:** Rating and review rows must **ONLY** render when non-null merchant review data is present.
- **Zero Fabrication:** Never invent, simulate, or hardcode fake ratings or fake review counts. If merchant data is absent, the row remains completely hidden.

---

## 6. Provisional Branding

- The name **"TiHin"** is final.
- The final brand name has not been locked.
- Permanent brand assets, logos, launcher icons, and splash identity for "TiHin" are locked and approved.

---

## 7. Universal Try-On Flow

There is exactly **one reusable Universal Try-On pipeline** across the entire application:

1. **Product Selection:** A product is selected *before* entering the Try-On screen (from Home hero showcase, Home trending, Discover catalog, or Price Tracking).
2. **Try-On Setup:** User confirms the selected garment and confirms or changes their personal portrait (via Camera or Photo Picker).
3. **Processing:** Full-screen animation with style tips and a cancel confirmation dialog.
4. **Result Screen:** Final hero result displayed with actions and purchase CTA.

---

## 8. Server-Side Credit System Architecture

- Credits are currently managed locally in memory for UI prototyping.
- Future server-side credit implementation must follow a **Credit Hold** pattern:
  - Initiating a try-on places 1 credit on temporary hold.
  - Successful generation commits the credit charge.
  - Failed or canceled try-ons must **refund/release** the hold immediately.
  - **Core Rule:** A failed or errored try-on must **never** permanently charge the user.

---

## 9. Hero Assets Preservation

- Manually provided hero assets and showcase carousel pairings must **never** be regenerated, overwritten, or replaced by automated tools.

---

## 10. Scope Discipline

- Implement strictly what is requested.
- Do not introduce unrequested features, tabs, integrations, or side projects.
- Preserve all locked decisions unless the user explicitly commands a change.
