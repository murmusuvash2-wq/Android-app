# Project State: OnMe (Provisional)

**Current Phase:** Frontend Prototype & UI Polish  
**Platform:** Android (Jetpack Compose, Material 3)  
**Last Updated:** September 2026

---

## 1. Project Purpose

OnMe is an AI-powered virtual outfit try-on Android application designed to let users visualize how clothing items look on them before buying. The app allows users to explore fashion items from various merchants, select or take a photo, simulate an AI try-on fitting, save and track looks, and find direct purchasing links.

---

## 2. Information Architecture & Navigation

The primary in-app navigation consists of a bottom navigation bar with four primary tabs:

```
[ Home ]  |  [ Looks ]  |  [ Discover ]  |  [ Me ]
```

### Screen Flow

```
Splash
  └── Onboarding (4 Slides)
        └── Auth Bottom Sheet (Google / Email / Guest)
              └── Main App Shell
                    ├── Home Tab
                    │     ├── Hero Showcase Carousel ("Try this look" → Try On)
                    │     ├── Quick Actions (Take Photo / Gallery → Try On)
                    │     └── Trending Looks Row (Card tap → Try On)
                    ├── Looks Tab
                    │     ├── Recent (Masonry Grid → Result)
                    │     ├── Favourites (Filtered Masonry Grid → Result)
                    │     └── Price Tracking (Tracked List → Try On)
                    ├── Discover Tab
                    │     ├── Search & Category Filter Pills
                    │     └── Staggered Product Cards ("Try On" → Try On, "Buy Now")
                    ├── Me Tab
                    │     ├── Profile Summary (Logged in vs. Guest)
                    │     ├── My Try-On Photos
                    │     ├── Activity & Settings
                    │     └── Account Actions (Log Out, Delete Account)
                    └── Dedicated Flows
                          ├── Try On (Product confirmation & user photo selection)
                          │     └── Processing (AI fitting animation & style tips)
                          │           └── Your New Look / Result (Hero result, actions, Buy ↗)
                          └── Credit Store (Purchase try-on credit packages)
```

---

## 3. Screen Implementations Breakdown

### Splash Screen (`SplashScreen` in `AuthScreens.kt`)
- **Status:** IMPLEMENTED (Local UI)
- **Features:** Displays animated pulsing brand logo and subtitle with automatic transition (1.8s delay) to the Onboarding screen.

### Onboarding Screen (`OnboardingScreen` in `AuthScreens.kt`)
- **Status:** IMPLEMENTED (Local UI)
- **Features:** 4-page `HorizontalPager` with indicator dots, backdrop imagery, and value propositions:
  1. *See it on you*
  2. *Discover your next look*
  3. *Try, Save, Shop*
  4. *Shop where you love*
- **Controls:** "Skip" button and dynamic "Next" / "Get Started" button that triggers the authentication bottom sheet.

### Authentication (`AuthBottomSheetContent` in `AuthScreens.kt`)
- **Status:** MOCK / LOCAL
- **Features:** Modal bottom sheet supporting three paths:
  - **Continue with Google:** Sets local session state (`isGuest = false`, assigns default credits).
  - **Sign Up / Sign In:** Basic form fields with local email/password validation and forgot password simulation.
  - **Continue as Guest:** Sets `SessionManager.isGuest = true` with zero persistence.
- **Backend Needed:** Real OAuth2 / Google Credential Manager and Firebase Authentication or custom auth backend.

### Home Tab (`HomeScreen.kt`)
- **Status:** IMPLEMENTED (Local Mock Data)
- **Features:**
  - Header with credit pill (hidden for guests, navigates to Credit Store).
  - "See the magic" hero showcase carousel: Side-by-side garment and model cards with auto-scroll and direct "Try this look" CTA.
  - Quick action buttons: "Take Photo" and "Choose Photo" via Android Photo Picker (`ActivityResultContracts.PickVisualMedia`).
  - "Try Trending Looks" horizontal feed with try-on count badges and "View All" link to Looks.

### Discover Tab (`DiscoverScreen.kt`)
- **Status:** IMPLEMENTED (Local Mock Catalog)
- **Features:**
  - Search input filtering mock products in real-time.
  - Filter pills: *Trending Now*, *Most Loved*, *Best Sellers*, *Just In*.
  - Staggered grid of editorial cards with merchant names, product titles, and localized INR (`₹`) prices.
  - Heart icon to toggle favorites (triggers account creation prompt for guests, shows undoable snackbar for registered users).
  - Direct "Try On" button on each card that loads that product directly into the try-on flow.
  - "Buy Now" link showing store redirect placeholder.

### Looks Tab (`LooksScreen.kt`)
- **Status:** IMPLEMENTED (Local In-Memory State)
- **Features:**
  - Tab 1: **Recent** — Masonry grid of generated looks with favorite toggling.
  - Tab 2: **Favourites** — Filtered masonry grid with empty state messaging.
  - Tab 3: **Price Tracking** — List of tracked items displaying price drop status badges.
  - Card tap opens the Result Screen with that look pre-loaded.
  - Guest users see clean empty states prompt to create an account.

### Me Tab (`MeScreen.kt`)
- **Status:** IMPLEMENTED (Local State)
- **Features:**
  - Conditional profile header for logged-in user vs. guest.
  - Guest upgrade CTA banner.
  - "My Try-On Photos" preview grid (3/5 photos).
  - Activity links to Saved Looks and Price Tracking.
  - Settings list with interactive "Price Drop Notifications" toggle switch.
  - Log Out and Delete Account actions.

### Universal Try-On Screen (`TryOnScreen` in `TryOnScreens.kt`)
- **Status:** IMPLEMENTED (Local State)
- **Features:**
  - Garment card showing image, brand, title, and price.
  - User photo container showing current portrait with "Change Photo" option, or empty state with "Add Your Photo".
  - Modal sheet to take a photo or select from device gallery.
  - Credit deduction indicator and "Confirm & Try On" CTA.

### Processing Screen (`ProcessingScreen` in `TryOnScreens.kt`)
- **Status:** MOCK / SIMULATED
- **Features:**
  - Pulsing AI fitting box animation with shimmer effect.
  - Rotating style and fashion tips card (cycles every 3 seconds).
  - BackHandler with confirmation dialog ("Cancel processing?").
  - 4.5-second simulated inference delay before transitioning to Result.

### Your New Look / Result Screen (`ResultScreen` in `TryOnScreens.kt`)
- **Status:** IMPLEMENTED
- **Features:**
  - Header: "Your New Look" with back navigation.
  - **Hero Area:** Large, standalone AI-generated result image (original photo kept in memory, not toggled by default).
  - Watermark badge ("OnMe AI") in corner.
  - Product metadata: Brand, product title, formatted price.
  - Rating/Reviews row: Schema-supported (`rating`, `reviewCount`, `ratingSource`), hidden cleanly when data is null.
  - Compact Actions Row:
    - `[Save]` (saves to Looks or prompts guest)
    - `[Download]` (toast confirmation)
    - `[Share]` (native Android `ACTION_SEND` Intent chooser)
    - `[🔔 Price Drop]` / `[🔔 Price Drop ✓]` (compact toggle updating local tracking state)
  - Primary Purchase CTA: `Buy ↗` (opens merchant store).
  - Secondary Options: "Remove Watermark · Watch Ad" toggle and "Try Another" return link.

### Credit Store (`CreditStoreScreen.kt`)
- **Status:** MOCK / LOCAL
- **Features:**
  - Packages: 10 Try-Ons (₹199), 50 Try-Ons (₹499, Most Popular), Unlimited Monthly (₹999).
  - Tapping packages updates local `SessionManager.credits` counter with toast feedback.
  - No real billing or payment gateway connected.

---

## 4. Current State Matrix

| Feature / Area | Status | Implementation Details |
| :--- | :--- | :--- |
| **UI Design & Layouts** | IMPLEMENTED | Fully responsive Jetpack Compose with Material 3 components |
| **Navigation & Routing** | IMPLEMENTED | Single-Activity `NavHost` with sealed `Screen` routes |
| **Photo Ingestion** | IMPLEMENTED | Android Photo Picker (`PickVisualMedia`) |
| **Native Sharing** | IMPLEMENTED | Android `ACTION_SEND` Intent chooser |
| **AI Try-On Generation** | MOCK / SIMULATED | 4.5s timer swapping image URI; no ML inference API connected |
| **User Credits System** | MOCK / LOCAL | In-memory `SessionManager.credits` integer |
| **Product Catalog** | MOCK / LOCAL | Static hardcoded Kotlin lists (`MOCK_DISCOVER_PRODUCTS`, etc.) |
| **Pricing & Currency** | MOCK / LOCAL | Hardcoded Double values formatted via `java.text.NumberFormat` in INR |
| **Looks Storage** | MOCK / LOCAL | In-memory `MOCK_RESULTS` list, resets on app restart |
| **Price Tracking Engine** | MOCK / LOCAL | Client-side boolean flags; no background price scraper or FCM push |
| **User Authentication** | MOCK / LOCAL | Client-side boolean `isGuest`; no OAuth or token exchange |
| **Monetization & Ads** | MOCK / LOCAL | Simulated click handlers; no Google Play Billing or AdMob |
| **Merchant Store Purchasing** | NOT IMPLEMENTED | Toast feedback; no affiliate API or browser deep linking |
| **Server-Side API / DB** | NOT IMPLEMENTED | No backend service, cloud database, or remote storage connected |

---

## 5. Automated Testing & Build Verification

- **Build Tool:** Gradle 9.3.1 with Android Gradle Plugin 9.1.1
- **Compilation:** `compile_applet` builds cleanly with zero errors.
- **Unit & Robolectric Tests:**
  - `:app:testDebugUnitTest` passes.
  - `ExampleRobolectricTest`: Verifies resource loading and app name.
  - `GreetingScreenshotTest`: Native graphics Robolectric test using Roborazzi rendering `SplashScreen`.
