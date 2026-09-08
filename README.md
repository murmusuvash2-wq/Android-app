# OnMe (Provisional)

> AI virtual outfit try-on Android application enabling users to visualize clothing items on themselves before purchasing.

---

## Current Status

**Frontend Prototype & UI Development**

The project is currently in the client-side prototyping phase. All primary screens, navigation flows, and interactive UI components are fully implemented and navigable in Jetpack Compose.

> **Important Integration Notice:**  
> Backend infrastructure is **not yet connected**.
> - **AI Generation:** The virtual try-on fitting process is currently **simulated** (timed delay swapping image URIs); no remote ML inference API is connected.
> - **Credits:** Managed via local in-memory state.
> - **Authentication:** Mocked client-side session (`isGuest` toggle); no OAuth or Firebase Auth is connected.
> - **Product Catalog & Prices:** Static local mock datasets.
> - **Price Tracking:** Client-side toggle without server scrapers or push notifications.
> - **Purchases:** Mocked feedback without Google Play Billing or affiliate gateways.

---

## Tech Stack

The application is built strictly with modern Android technologies present in the repository:

- **Language:** Kotlin (2.2.10)
- **UI Framework:** Jetpack Compose (Compose BOM 2024.09.00)
- **Design System:** Material Design 3 (Material3)
- **Navigation:** Jetpack Navigation Compose
- **Image Loading:** Coil (`io.coil-kt:coil-compose:2.7.0`)
- **Concurrency:** Kotlin Coroutines & Flow
- **Build System:** Gradle 9.3.1 with Android Gradle Plugin (AGP) 9.1.1
- **Testing:** JUnit 4, Robolectric (SDK 36), Roborazzi screenshot verification

---

## Project Structure

```
├── app/
│   ├── build.gradle.kts                   # App module dependencies & SDK config
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml        # App declarations, permissions & SEND intent
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt        # Entry point activity handling image intents
│       │   │   └── ui/
│       │   │       ├── AppNavigation.kt   # Central navigation graph & routes
│       │   │       ├── AppShell.kt        # Bottom navigation bar & scaffold wrapper
│       │   │       ├── AuthScreens.kt     # Splash, Onboarding pager & Auth bottom sheet
│       │   │       ├── CreditStoreScreen.kt # Try-on credit package store
│       │   │       ├── DiscoverScreen.kt  # Editorial catalog & category search
│       │   │       ├── HomeScreen.kt      # Hero showcase, photo shortcuts & trending
│       │   │       ├── LooksScreen.kt     # Recent, Favourites & Price Tracking masonry
│       │   │       ├── MeScreen.kt        # Profile, photo management & settings
│       │   │       ├── SessionManager.kt  # Global session & credit state
│       │   │       ├── TryOnScreens.kt    # TryOn, Processing & Result (Your New Look)
│       │   │       └── theme/             # Color tokens, typography & M3 theme setup
│       │   └── res/                       # Drawables, mipmaps, strings & launcher icons
│       └── test/                          # Unit & Robolectric screenshot test suite
├── DECISIONS.md                           # Locked product & architecture decisions
├── DESIGN_SYSTEM.md                       # Intended color palette, tokens & design rules
├── PROJECT_STATE.md                       # Comprehensive status & implementation audit
└── README.md                              # This document
```

---

## Implemented User Flows

1. **Onboarding & Authentication:**
   - Animated Splash Screen $\rightarrow$ 4-slide Onboarding Pager $\rightarrow$ Modal Auth Sheet (Google / Email / Guest mode).
2. **Fashion Discovery:**
   - Category filtering (*Trending Now*, *Most Loved*, *Best Sellers*, *Just In*) and real-time title search.
   - Direct "Try On" launch per garment.
3. **Universal Virtual Try-On Pipeline:**
   - Selected garment review paired with personal photo capture (Camera or Android Photo Picker).
   - Animated AI Processing view with contextual style tips and cancel confirmation.
   - Result screen ("Your New Look") featuring standalone hero AI result, conditional merchant reviews, compact actions (`Save`, `Download`, `Share`, `🔔 Price Drop`), and primary `Buy ↗` CTA.
4. **Looks & Tracking:**
   - Filterable masonry view of generated looks (Recent vs. Favourites) and tracked price drop items.
5. **Profile & Credit Management:**
   - Account settings, guest-to-account upgrade prompts, and credit store package selection.

---

## Build & Verification

The project compiles cleanly and includes local unit and screenshot verification tests:

- **Compile:** Verified via Gradle compilation (`compile_applet`).
- **Run Unit Tests:**
  ```bash
  gradle :app:testDebugUnitTest
  ```
- **Screenshot Tests:** Uses Robolectric with Roborazzi (`GreetingScreenshotTest`) for headless UI verification without an emulator.

---

## Future Roadmap

The following sequential milestones are required for full production readiness:

```
Backend Architecture 
  └── Authentication (Google Credential Manager / Firebase Auth)
        └── Product Catalog API (Real merchant feeds & search)
              └── Cloud Storage (User portrait & generated look assets)
                    └── Server-Side Credits (Transaction ledger & hold/spend model)
                          └── AI Virtual Try-On Engine (Cloud ML inference pipeline)
                                └── Price Tracking Engine (Scrapers & automated webhooks)
                                      └── Push Notifications (Firebase Cloud Messaging)
                                            └── Monetization (Google Play Billing & AdMob)
                                                  └── Production End-to-End Testing
```
