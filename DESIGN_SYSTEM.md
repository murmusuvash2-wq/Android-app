# Design System & Visual Guidelines

This document specifies the design principles, color palette, typography hierarchy, and UI component standards for the application.

---

## 1. Core Visual Principles

The application adopts a **premium, minimal, warm, fashion-editorial visual language** inspired by modern luxury e-commerce and editorial magazines:

- **Warm Creamy Canvas:** Screens must use a soft, warm ivory background rather than stark blinding white or cool tech gray.
- **Pure White Surfaces:** Cards, sheets, dialogs, and elevated components use crisp white framed by hairline borders for depth.
- **Rich Charcoal Typography:** Primary titles, body text, and headlines use high-contrast rich charcoal rather than pure `#000000`.
- **Restrained Accents:**
  - **Indigo Violet** is an intentional, selective accent for primary states, active selection, and AI features. It must **never** saturate entire backgrounds, large card containers, or dominant button clusters.
  - **Champagne Gold** is used sparingly for premium badges, credit balances, and verified indicators. Never use garish amber or bright arcade orange.
- **Hairline Framing:** Subtle 1dp warm borders provide spatial definition without heavy drop shadows.

---

## 2. Color Palette Specification

### Palette Definition

| Role / Token Name | Hex Value | Color Swatch Name | Description |
| :--- | :--- | :--- | :--- |
| **Background** | `#FAF9F6` | Warm Ivory | The global app background canvas |
| **Surface** | `#FFFFFF` | Pure White | Elevated cards, bottom sheets, navigation bars |
| **Surface Variant** | `#F3F1ED` | Warm Gray / Ecru | Inactive tabs, secondary containers, photo backdrops |
| **Primary Accent** | `#6B46C1` | Indigo Violet | Brand accent, active tab pills, selected state indicators |
| **Primary Text** | `#1E1E24` | Rich Charcoal | Headings, titles, primary labels, high-contrast text |
| **Secondary Text** | `#6B7280` | Muted Slate | Subtitles, product descriptions, secondary actions |
| **Tertiary Text** | `#9CA3AF` | Cool Gray | Timestamps, placeholder text, hints |
| **Champagne Gold** | `#C9A961` | Champagne Gold | Credit badges, special status pills, premium markers |
| **Border** | `#E5E1DC` | Soft Sandstone | Card borders, dividers, chip outlines (1dp) |
| **Primary Purchase CTA** | `#1E1E24` | Charcoal | Primary high-conversion action button (`Buy ↗`) |
| **CTA Text** | `#FFFFFF` | Pure White | Text and icons inside dark primary buttons |

---

## 3. Usage Rules: Where Each Color Belongs

### Background (`#FAF9F6`)
- Root composable containers in `Scaffold` or screen-level `Box`/`Column`.
- Inset spacing and padding channels between grid elements.
- Underlay for scrollable content areas.

### Surface (`#FFFFFF`)
- Product cards in Discover and Home feeds.
- Bottom navigation bar container.
- Modal bottom sheets (Authentication, Photo Selection).
- Dialog containers and alert modals.
- Interactive action buttons in secondary rows (`Save`, `Download`, `Share`, `🔔 Price Drop`).

### Surface Variant (`#F3F1ED`)
- Image placeholder boxes during loading or empty states.
- Inactive filter pill backgrounds.
- Search input field background.
- Subtle inner grouping containers.

### Primary Accent (`#6B46C1` - Indigo Violet)
- Active selected filter chips (e.g., active category in Discover).
- Selected bottom navigation tab icon and label indicator.
- Subtle AI sparkle indicators and active processing animations.
- Active toggle states (e.g., Price Drop active state background tint).
- *Forbidden:* Do NOT use as fullscreen backgrounds, full-bleed hero banners, or body text.

### Primary Text (`#1E1E24` - Rich Charcoal)
- Large display titles (e.g., screen headers "Discover", "Looks", "Me").
- Garment product titles and prices.
- Primary button labels on dark buttons.
- Standard body text.

### Secondary Text (`#6B7280`)
- Brand/merchant labels (e.g., "ZARA", "MANGO").
- Section subtitles and onboarding descriptions.
- Secondary button text.

### Tertiary Text (`#9CA3AF`)
- Timestamps (e.g., "2h ago", "Yesterday").
- Photo capacity metrics (e.g., "3 / 5 photos").
- Inactive field placeholders.

### Champagne Gold (`#C9A961`)
- Credit balance badge in the top navigation bar.
- Credit package highlight pills ("MOST POPULAR").
- Optional rating star icon when merchant review data is present.
- *Forbidden:* Do NOT use for standard buttons, body text, or large surfaces.

### Border (`#E5E1DC`)
- 1dp outlines on all cards (`CardDefaults.cardColors` paired with `BorderStroke(1.dp, Border)`).
- `HorizontalDivider` dividers between list rows in settings.
- Text field unselected border outlines.

### Primary Purchase CTA (`#1E1E24`) & CTA Text (`#FFFFFF`)
- Dedicated to the highest-priority conversion element: the `Buy ↗` button on the Result screen and primary step transitions.

---

## 4. Centralization & Token Architecture

All colors must be referenced exclusively through Compose theme tokens (`MaterialTheme.colorScheme.*` or centralized semantic color objects in `Color.kt` and `Theme.kt`).

- **No Hardcoded Hex Values in Composables:** Composable functions should never define raw `Color(0xFF...)` values inline.
- **Semantic Mapping:**
  - `colorScheme.background` $\rightarrow$ `#FAF9F6`
  - `colorScheme.surface` $\rightarrow$ `#FFFFFF`
  - `colorScheme.surfaceVariant` $\rightarrow$ `#F3F1ED`
  - `colorScheme.primary` $\rightarrow$ `#6B46C1`
  - `colorScheme.onBackground` $\rightarrow$ `#1E1E24`
  - `colorScheme.onSurface` $\rightarrow$ `#1E1E24`
  - `colorScheme.outline` $\rightarrow$ `#E5E1DC`

*(Note: This document records the finalized target design system. The underlying theme code remains intact until explicitly scheduled for theme migration.)*
