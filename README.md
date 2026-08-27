<div align="center">

<!-- Banner Image -->
<img src="assets/banner.jpg" alt="CacheDeal Banner" width="100%"/>

<br/>
<br/>

<!-- Badges -->
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Supabase](https://img.shields.io/badge/Supabase-Backend-FFCA28?style=for-the-badge&logo=Supabase&logoColor=black)](https://Supabase.google.com/)
[![License](https://img.shields.io/badge/License-MIT-00C853?style=for-the-badge)](LICENSE)

<br/>

[![Build Status](https://img.shields.io/github/actions/workflow/status/redrighthand2007/CacheDeal-App/android-ci.yml?branch=main&style=flat-square&label=CI&logo=githubactions)](https://github.com/redrighthand2007/CacheDeal-App/actions)
[![Issues](https://img.shields.io/github/issues/redrighthand2007/CacheDeal-App?style=flat-square&color=FF6B6B)](https://github.com/redrighthand2007/CacheDeal-App/issues)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen?style=flat-square)](CONTRIBUTING.md)
[![Stars](https://img.shields.io/github/stars/redrighthand2007/CacheDeal-App?style=flat-square&color=FFD700)](https://github.com/redrighthand2007/CacheDeal-App/stargazers)

---

### ðŸ”’ The campus marketplace where deals get locked.

**Buy. Sell. Deal. On Campus.**

A peer-to-peer marketplace built exclusively for **VIT Vellore** students to buy and sell items â€” cycles, calculators, lab coats, subscriptions, notes, game accounts â€” all within the campus.

[ðŸ“± Download APK](#-download) Â· [ðŸ“– Documentation](docs/) Â· [ðŸ› Report Bug](.github/ISSUE_TEMPLATE/bug_report.md) Â· [âœ¨ Request Feature](.github/ISSUE_TEMPLATE/feature_request.md)

</div>

---

## âš¡ How It Works

```
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”     â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”     â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”     â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚   ðŸ“¦ LIST   â”‚â”€â”€â”€â”€â–¶â”‚  ðŸ’° OFFER    â”‚â”€â”€â”€â”€â–¶â”‚  ðŸ”’ LOCK    â”‚â”€â”€â”€â”€â–¶â”‚  ðŸ¤ DEAL     â”‚
â”‚  Post your  â”‚     â”‚  Buyers make â”‚     â”‚  Seller     â”‚     â”‚  Meet up &   â”‚
â”‚  item with  â”‚     â”‚  cash offers â”‚     â”‚  picks the  â”‚     â”‚  complete    â”‚
â”‚  a price    â”‚     â”‚  with notes  â”‚     â”‚  best offer â”‚     â”‚  via WhatsAppâ”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜     â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜     â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜     â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
```

<div align="center">

| Step | What Happens |
|:----:|:------------|
| **1** | ðŸ“¸ Seller posts an item with photo, price & category |
| **2** | ðŸ” Buyers browse by category or "Near Me" filter |
| **3** | ðŸ’¸ Interested buyers submit cash offers with optional notes |
| **4** | âš–ï¸ Seller reviews offers sorted by amount, sees buyer reputation |
| **5** | âœ… Seller accepts one offer â†’ deal locks atomically |
| **6** | ðŸ“± Both get a WhatsApp deep link to arrange handoff |
| **7** | ðŸŸ¢ Both confirm within 3 days â†’ green dots for reliability |

</div>

---

## âœ¨ Features

<div align="center">

|  | Feature | Description |
|:---:|:--------|:------------|
| ðŸ“± | **Phone OTP Auth** | Quick signup with phone verification â€” no complex forms |
| ðŸ“¦ | **Smart Listings** | Post items with photo, price, description & category |
| ðŸ·ï¸ | **8 Categories** | Eatables Â· Wearables Â· Cycles Â· Calculators Â· Lab Coats Â· Subscriptions Â· Study Notes Â· Game Accounts |
| ðŸ“ | **Near Me Filter** | Find items from your hostel block or nearby |
| ðŸ’° | **Cash Offers** | Submit offers with optional notes for negotiation context |
| ðŸ”’ | **Atomic Deal Lock** | One-tap accept â€” atomically locks deal, rejects others |
| ðŸ’¬ | **WhatsApp Connect** | Pre-filled WhatsApp message to arrange the meetup |
| ðŸŸ¢ðŸ”´ | **Reputation Dots** | Green dots for completed deals, red dots for no-shows |
| â° | **3-Day Window** | Completion deadline keeps deals moving |
| ðŸ”„ | **Auto Re-list** | Missed deals auto-expire, item goes back to market |

</div>

---

## ðŸ›¡ï¸ Reputation System

The trust system is **automatic and abuse-resistant** â€” no manual reporting needed.

```
  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
  â”‚                 Deal Locked â±ï¸ 3 Days                â”‚
  â”‚                                                     â”‚
  â”‚    â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”          â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”   â”‚
  â”‚    â”‚ Both confirm â”‚          â”‚ Buyer doesn't    â”‚   â”‚
  â”‚    â”‚ completion   â”‚          â”‚ confirm in time  â”‚   â”‚
  â”‚    â””â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”˜          â””â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â”‚
  â”‚           â”‚                           â”‚             â”‚
  â”‚     ðŸŸ¢ +1 Green Dot            Seller re-lists     â”‚
  â”‚     to BOTH users              the item             â”‚
  â”‚                                       â”‚             â”‚
  â”‚                                 ðŸ”´ +1 Red Dot       â”‚
  â”‚                                 to BUYER only       â”‚
  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
```

- **Green Dot ðŸŸ¢** â€” Both sides completed the deal. Shows reliability.
- **Red Dot ðŸ”´** â€” Buyer failed to show up. Only triggered by seller's re-list action.
- Dots are visible on profiles and next to each offer, so sellers can weigh a lower offer from a reliable buyer against a higher offer from a flaky one.

---

## ðŸ—ï¸ Tech Stack

<div align="center">

| Layer | Technology | Purpose |
|:------|:-----------|:--------|
| **Language** | ![Kotlin](https://img.shields.io/badge/Kotlin-2.1+-7F52FF?style=flat-square&logo=kotlin&logoColor=white) | Modern, concise, null-safe |
| **UI** | ![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white) | Declarative UI with Material You |
| **Architecture** | MVVM + Clean Architecture | Separation of concerns |
| **DI** | ![Hilt](https://img.shields.io/badge/Hilt-Dagger-FF6F00?style=flat-square) | Dependency injection |
| **Navigation** | Compose Navigation 2.8+ | Type-safe `@Serializable` routes |
| **Auth** | ![Supabase](https://img.shields.io/badge/Supabase-Phone%20Auth-FFCA28?style=flat-square&logo=Supabase&logoColor=black) | Phone OTP verification |
| **Database** | ![Postgres](https://img.shields.io/badge/Cloud-Postgres-FFCA28?style=flat-square&logo=Supabase&logoColor=black) | Real-time NoSQL database |
| **Cloudinary** | ![Cloudinary](https://img.shields.io/badge/Supabase-Cloudinary-FFCA28?style=flat-square&logo=Supabase&logoColor=black) | Photo uploads |
| **Images** | ![Coil](https://img.shields.io/badge/Coil%203-Image%20Loading-00BCD4?style=flat-square) | Fast async image loading |
| **Async** | Coroutines + Flow | Reactive data streams |
| **CI/CD** | ![GitHub Actions](https://img.shields.io/badge/GitHub-Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white) | Automated build & test |

</div>

---

## ðŸ›ï¸ Architecture

```
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚                       UI LAYER                           â”‚
â”‚                                                          â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”   â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”   â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”‚
â”‚  â”‚ Screens  â”‚â—„â”€â”€â”¤ ViewModelsâ”‚â”€â”€â”€â”¤ UiState / UiEvents â”‚  â”‚
â”‚  â”‚(Compose) â”‚   â”‚(StateFlow)â”‚   â”‚ (Sealed Classes)   â”‚  â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â””â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”˜   â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜  â”‚
â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
â”‚                  DOMAIN LAYER                            â”‚
â”‚                        â”‚                                 â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”   â”‚
â”‚  â”‚                  Use Cases                        â”‚   â”‚
â”‚  â”‚  PostItemUseCase â”‚ AcceptOfferUseCase â”‚ SendOtp   â”‚   â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”   â”‚
â”‚  â”‚            Repository Interfaces                  â”‚   â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â”‚
â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
â”‚                   DATA LAYER                             â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”   â”‚
â”‚  â”‚          Repository Implementations               â”‚   â”‚
â”‚  â””â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â”‚
â”‚  â”Œâ”€â”€â”€â”´â”€â”€â”€â”€â”   â”Œâ”€â”€â”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â”  â”Œâ”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”          â”‚
â”‚  â”‚Supabaseâ”‚   â”‚ Postgres  â”‚  â”‚   Supabase   â”‚          â”‚
â”‚  â”‚  Auth  â”‚   â”‚  Database  â”‚  â”‚   Cloudinary    â”‚          â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜          â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
```

> For detailed architecture documentation, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

---

## ðŸ“‚ Project Structure

```
com.vit.cachedeal/
â”œâ”€â”€ ðŸ“± App.kt & MainActivity.kt
â”‚
â”œâ”€â”€ ðŸŽ¨ core/
â”‚   â”œâ”€â”€ designsystem/          # Theme, colors, typography, components
â”‚   â”‚   â”œâ”€â”€ theme/             # Material 3 theme tokens
â”‚   â”‚   â””â”€â”€ component/         # ItemCard, DotBadge, CategoryChip...
â”‚   â”œâ”€â”€ di/                    # Hilt modules (Supabase, Repos)
â”‚   â”œâ”€â”€ model/                 # User, Item, Offer, Deal, Category
â”‚   â””â”€â”€ util/                  # Resource, Constants, WhatsAppHelper
â”‚
â”œâ”€â”€ ðŸ’¾ data/
â”‚   â”œâ”€â”€ repository/            # Supabase implementations
â”‚   â””â”€â”€ source/                # Supabase data sources
â”‚
â”œâ”€â”€ ðŸ§  domain/
â”‚   â”œâ”€â”€ repository/            # Repository interfaces
â”‚   â””â”€â”€ usecase/               # Business logic (PostItem, AcceptOffer...)
â”‚
â””â”€â”€ ðŸ–¥ï¸ ui/
    â”œâ”€â”€ navigation/            # Routes & NavHost
    â”œâ”€â”€ auth/                  # Phone OTP login
    â”œâ”€â”€ onboarding/            # Name + block setup
    â”œâ”€â”€ home/                  # Category browse feed
    â”œâ”€â”€ postitem/              # List an item
    â”œâ”€â”€ itemdetail/            # View item + make offer
    â”œâ”€â”€ mylistings/            # Seller's items
    â”œâ”€â”€ offers/                # Offer review screen
    â”œâ”€â”€ deals/                 # Active deals management
    â””â”€â”€ profile/               # User profile + reputation
```

---

## ðŸš€ Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 17+**
- **Android SDK 35**
- A **Supabase project** with Phone Auth, Postgres, and Cloudinary enabled

### Quick Start

```bash
# 1. Clone the repo
git clone https://github.com/redrighthand2007/CacheDeal-App.git
cd CacheDeal-App

# 2. Add your Supabase config
# Download google-services.json from Supabase Console
# Place it in the app/ directory

# 3. Open in Android Studio & run!
```

> ðŸ“– For detailed setup instructions, see [docs/SETUP.md](docs/SETUP.md)

---

## ðŸ—„ï¸ Database Schema

<details>
<summary><b>Click to expand Postgres schema</b></summary>

### Users (`users/{uid}`)
| Field | Type | Description |
|-------|------|-------------|
| `phone` | string | Verified phone number |
| `name` | string | Display name |
| `block` | string | Hostel/block |
| `greenDots` | int | Completed deals count |
| `redDots` | int | Missed completions count |
| `createdAt` | timestamp | Account creation |

### Items (`items/{id}`)
| Field | Type | Description |
|-------|------|-------------|
| `sellerId` | string | Seller's UID |
| `category` | string | Fixed category |
| `title` | string | Item title |
| `description` | string | Description |
| `price` | number | Asking price |
| `photoUrl` | string | Cloudinary URL |
| `status` | string | `open` / `locked` / `sold` |

### Offers (`items/{id}/offers/{offerId}`)
| Field | Type | Description |
|-------|------|-------------|
| `buyerId` | string | Buyer's UID |
| `amount` | number | Cash offer |
| `note` | string? | Optional message |
| `status` | string | `pending` / `accepted` / `rejected` |

### Deals (`deals/{id}`)
| Field | Type | Description |
|-------|------|-------------|
| `itemId` | string | Item reference |
| `sellerId` | string | Seller's UID |
| `buyerId` | string | Buyer's UID |
| `finalPrice` | number | Accepted amount |
| `completionDeadline` | timestamp | `lockedAt + 3 days` |
| `status` | string | `locked` / `completed` / `expired` |

</details>

> ðŸ“– For full schema documentation, see [docs/Postgres_SCHEMA.md](docs/Postgres_SCHEMA.md)

---

## ðŸ—ºï¸ What We've Built (So Far)

We're moving fast. Here's where CacheDeal currently stands:

- [x] ðŸŽ¨ **Sleek UI Architecture:** Beautiful Jetpack Compose components.
- [x] ðŸ“± **Full App Screens:** Auth, Home, Profile, Sell, and Deals grids are fully designed.
- [x] ðŸŒ™ **Dynamic Themes:** Smooth 3-way toggle between Light, Dark, and System modes.
- [x] ðŸš€ **Lightning Fast Launch:** Zero-delay native Android 12 splash screen.
- [x] ðŸ“¦ **Data Models & Repos:** Full MVVM + Clean Architecture scaffolding.
- [ ] ðŸ”Œ **Backend Hookup:** Wiring up Supabase (or Supabase!) for real-time auth and data.
- [ ] ðŸ’¬ **WhatsApp Deep Links:** Seamless handoffs for meetups.
- [ ] ðŸ§ª **Campus Beta Test:** Launching to our first batch of students.

---

## ðŸ¤ Contributing

Contributions are what make the open-source community an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/amazing-feature`)
3. Commit your Changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the Branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

> ðŸ“– See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

---

## ðŸ“„ License

Distributed under the **MIT License**. See [LICENSE](LICENSE) for more information.

---

## ðŸ™ Acknowledgements

- [Jetpack Compose](https://developer.android.com/jetpack/compose) â€” Modern declarative UI
- [Supabase](https://Supabase.google.com/) â€” Backend infrastructure
- [Material 3](https://m3.material.io/) â€” Design system
- [Coil](https://coil-kt.github.io/coil/) â€” Image loading
- [Hilt](https://dagger.dev/hilt/) â€” Dependency injection
- The **VIT Vellore** student community ðŸ’›

---

<div align="center">

**Built with â¤ï¸ for VIT Vellore Campus**

<img src="assets/logo.jpg" alt="CacheDeal Logo" width="80"/>

<br/>

â­ **Star this repo if you find it useful!** â­

</div>


