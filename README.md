<div align="center">

<!-- Banner Image -->
<img src="assets/banner.jpg" alt="CacheDeal Banner" width="100%"/>

<br/>
<br/>

<!-- Badges -->
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Backend-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![License](https://img.shields.io/badge/License-MIT-00C853?style=for-the-badge)](LICENSE)

<br/>

[![Build Status](https://img.shields.io/github/actions/workflow/status/redrighthand2007/CacheDeal-App/android-ci.yml?branch=main&style=flat-square&label=CI&logo=githubactions)](https://github.com/redrighthand2007/CacheDeal-App/actions)
[![Issues](https://img.shields.io/github/issues/redrighthand2007/CacheDeal-App?style=flat-square&color=FF6B6B)](https://github.com/redrighthand2007/CacheDeal-App/issues)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen?style=flat-square)](CONTRIBUTING.md)
[![Stars](https://img.shields.io/github/stars/redrighthand2007/CacheDeal-App?style=flat-square&color=FFD700)](https://github.com/redrighthand2007/CacheDeal-App/stargazers)

---

### 🔒 The campus marketplace where deals get locked.

**Buy. Sell. Deal. On Campus.**

A peer-to-peer marketplace built exclusively for **VIT Vellore** students to buy and sell items — cycles, calculators, lab coats, subscriptions, notes, game accounts — all within the campus.

[📱 Download APK](#-download) · [📖 Documentation](docs/) · [🐛 Report Bug](.github/ISSUE_TEMPLATE/bug_report.md) · [✨ Request Feature](.github/ISSUE_TEMPLATE/feature_request.md)

</div>

---

## ⚡ How It Works

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────────┐
│   📦 LIST   │────▶│  💰 OFFER    │────▶│  🔒 LOCK    │────▶│  🤝 DEAL     │
│  Post your  │     │  Buyers make │     │  Seller     │     │  Meet up &   │
│  item with  │     │  cash offers │     │  picks the  │     │  complete    │
│  a price    │     │  with notes  │     │  best offer │     │  via WhatsApp│
└─────────────┘     └──────────────┘     └─────────────┘     └──────────────┘
```

<div align="center">

| Step | What Happens |
|:----:|:------------|
| **1** | 📸 Seller posts an item with photo, price & category |
| **2** | 🔍 Buyers browse by category or "Near Me" filter |
| **3** | 💸 Interested buyers submit cash offers with optional notes |
| **4** | ⚖️ Seller reviews offers sorted by amount, sees buyer reputation |
| **5** | ✅ Seller accepts one offer → deal locks atomically |
| **6** | 📱 Both get a WhatsApp deep link to arrange handoff |
| **7** | 🟢 Both confirm within 3 days → green dots for reliability |

</div>

---

## ✨ Features

<div align="center">

|  | Feature | Description |
|:---:|:--------|:------------|
| 📱 | **Phone OTP Auth** | Quick signup with phone verification — no complex forms |
| 📦 | **Smart Listings** | Post items with photo, price, description & category |
| 🏷️ | **8 Categories** | Eatables · Wearables · Cycles · Calculators · Lab Coats · Subscriptions · Study Notes · Game Accounts |
| 📍 | **Near Me Filter** | Find items from your hostel block or nearby |
| 💰 | **Cash Offers** | Submit offers with optional notes for negotiation context |
| 🔒 | **Atomic Deal Lock** | One-tap accept — atomically locks deal, rejects others |
| 💬 | **WhatsApp Connect** | Pre-filled WhatsApp message to arrange the meetup |
| 🟢🔴 | **Reputation Dots** | Green dots for completed deals, red dots for no-shows |
| ⏰ | **3-Day Window** | Completion deadline keeps deals moving |
| 🔄 | **Auto Re-list** | Missed deals auto-expire, item goes back to market |

</div>

---

## 🛡️ Reputation System

The trust system is **automatic and abuse-resistant** — no manual reporting needed.

```
  ┌─────────────────────────────────────────────────────┐
  │                 Deal Locked ⏱️ 3 Days                │
  │                                                     │
  │    ┌──────────────┐          ┌──────────────────┐   │
  │    │ Both confirm │          │ Buyer doesn't    │   │
  │    │ completion   │          │ confirm in time  │   │
  │    └──────┬───────┘          └────────┬─────────┘   │
  │           │                           │             │
  │     🟢 +1 Green Dot            Seller re-lists     │
  │     to BOTH users              the item             │
  │                                       │             │
  │                                 🔴 +1 Red Dot       │
  │                                 to BUYER only       │
  └─────────────────────────────────────────────────────┘
```

- **Green Dot 🟢** — Both sides completed the deal. Shows reliability.
- **Red Dot 🔴** — Buyer failed to show up. Only triggered by seller's re-list action.
- Dots are visible on profiles and next to each offer, so sellers can weigh a lower offer from a reliable buyer against a higher offer from a flaky one.

---

## 🏗️ Tech Stack

<div align="center">

| Layer | Technology | Purpose |
|:------|:-----------|:--------|
| **Language** | ![Kotlin](https://img.shields.io/badge/Kotlin-2.1+-7F52FF?style=flat-square&logo=kotlin&logoColor=white) | Modern, concise, null-safe |
| **UI** | ![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white) | Declarative UI with Material You |
| **Architecture** | MVVM + Clean Architecture | Separation of concerns |
| **DI** | ![Hilt](https://img.shields.io/badge/Hilt-Dagger-FF6F00?style=flat-square) | Dependency injection |
| **Navigation** | Compose Navigation 2.8+ | Type-safe `@Serializable` routes |
| **Auth** | ![Firebase](https://img.shields.io/badge/Firebase-Phone%20Auth-FFCA28?style=flat-square&logo=firebase&logoColor=black) | Phone OTP verification |
| **Database** | ![Firestore](https://img.shields.io/badge/Cloud-Firestore-FFCA28?style=flat-square&logo=firebase&logoColor=black) | Real-time NoSQL database |
| **Storage** | ![Storage](https://img.shields.io/badge/Firebase-Storage-FFCA28?style=flat-square&logo=firebase&logoColor=black) | Photo uploads |
| **Images** | ![Coil](https://img.shields.io/badge/Coil%203-Image%20Loading-00BCD4?style=flat-square) | Fast async image loading |
| **Async** | Coroutines + Flow | Reactive data streams |
| **CI/CD** | ![GitHub Actions](https://img.shields.io/badge/GitHub-Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white) | Automated build & test |

</div>

---

## 🏛️ Architecture

```
┌──────────────────────────────────────────────────────────┐
│                       UI LAYER                           │
│                                                          │
│  ┌──────────┐   ┌───────────┐   ┌────────────────────┐  │
│  │ Screens  │◄──┤ ViewModels│───┤ UiState / UiEvents │  │
│  │(Compose) │   │(StateFlow)│   │ (Sealed Classes)   │  │
│  └──────────┘   └─────┬─────┘   └────────────────────┘  │
├────────────────────────┼─────────────────────────────────┤
│                  DOMAIN LAYER                            │
│                        │                                 │
│  ┌─────────────────────┴─────────────────────────────┐   │
│  │                  Use Cases                        │   │
│  │  PostItemUseCase │ AcceptOfferUseCase │ SendOtp   │   │
│  └─────────────────────┬─────────────────────────────┘   │
│  ┌─────────────────────┴─────────────────────────────┐   │
│  │            Repository Interfaces                  │   │
│  └─────────────────────┬─────────────────────────────┘   │
├────────────────────────┼─────────────────────────────────┤
│                   DATA LAYER                             │
│  ┌─────────────────────┴─────────────────────────────┐   │
│  │          Repository Implementations               │   │
│  └───┬──────────────┬──────────────┬─────────────────┘   │
│  ┌───┴────┐   ┌─────┴──────┐  ┌───┴──────────┐          │
│  │Firebase│   │ Firestore  │  │   Firebase   │          │
│  │  Auth  │   │  Database  │  │   Storage    │          │
│  └────────┘   └────────────┘  └──────────────┘          │
└──────────────────────────────────────────────────────────┘
```

> For detailed architecture documentation, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

---

## 📂 Project Structure

```
com.vit.cachedeal/
├── 📱 App.kt & MainActivity.kt
│
├── 🎨 core/
│   ├── designsystem/          # Theme, colors, typography, components
│   │   ├── theme/             # Material 3 theme tokens
│   │   └── component/         # ItemCard, DotBadge, CategoryChip...
│   ├── di/                    # Hilt modules (Firebase, Repos)
│   ├── model/                 # User, Item, Offer, Deal, Category
│   └── util/                  # Resource, Constants, WhatsAppHelper
│
├── 💾 data/
│   ├── repository/            # Firebase implementations
│   └── source/                # Firebase data sources
│
├── 🧠 domain/
│   ├── repository/            # Repository interfaces
│   └── usecase/               # Business logic (PostItem, AcceptOffer...)
│
└── 🖥️ ui/
    ├── navigation/            # Routes & NavHost
    ├── auth/                  # Phone OTP login
    ├── onboarding/            # Name + block setup
    ├── home/                  # Category browse feed
    ├── postitem/              # List an item
    ├── itemdetail/            # View item + make offer
    ├── mylistings/            # Seller's items
    ├── offers/                # Offer review screen
    ├── deals/                 # Active deals management
    └── profile/               # User profile + reputation
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 17+**
- **Android SDK 35**
- A **Firebase project** with Phone Auth, Firestore, and Storage enabled

### Quick Start

```bash
# 1. Clone the repo
git clone https://github.com/redrighthand2007/CacheDeal-App.git
cd CacheDeal-App

# 2. Add your Firebase config
# Download google-services.json from Firebase Console
# Place it in the app/ directory

# 3. Open in Android Studio & run!
```

> 📖 For detailed setup instructions, see [docs/SETUP.md](docs/SETUP.md)

---

## 🗄️ Database Schema

<details>
<summary><b>Click to expand Firestore schema</b></summary>

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
| `photoUrl` | string | Storage URL |
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

> 📖 For full schema documentation, see [docs/FIRESTORE_SCHEMA.md](docs/FIRESTORE_SCHEMA.md)

---

## 🗺️ What We've Built (So Far)

We're moving fast. Here's where CacheDeal currently stands:

- [x] 🎨 **Sleek UI Architecture:** Beautiful Jetpack Compose components.
- [x] 📱 **Full App Screens:** Auth, Home, Profile, Sell, and Deals grids are fully designed.
- [x] 🌙 **Dynamic Themes:** Smooth 3-way toggle between Light, Dark, and System modes.
- [x] 🚀 **Lightning Fast Launch:** Zero-delay native Android 12 splash screen.
- [x] 📦 **Data Models & Repos:** Full MVVM + Clean Architecture scaffolding.
- [ ] 🔌 **Backend Hookup:** Wiring up Firebase (or Supabase!) for real-time auth and data.
- [ ] 💬 **WhatsApp Deep Links:** Seamless handoffs for meetups.
- [ ] 🧪 **Campus Beta Test:** Launching to our first batch of students.

---

## 🤝 Contributing

Contributions are what make the open-source community an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/amazing-feature`)
3. Commit your Changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the Branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

> 📖 See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

---

## 📄 License

Distributed under the **MIT License**. See [LICENSE](LICENSE) for more information.

---

## 🙏 Acknowledgements

- [Jetpack Compose](https://developer.android.com/jetpack/compose) — Modern declarative UI
- [Firebase](https://firebase.google.com/) — Backend infrastructure
- [Material 3](https://m3.material.io/) — Design system
- [Coil](https://coil-kt.github.io/coil/) — Image loading
- [Hilt](https://dagger.dev/hilt/) — Dependency injection
- The **VIT Vellore** student community 💛

---

<div align="center">

**Built with ❤️ for VIT Vellore Campus**

<img src="assets/logo.jpg" alt="CacheDeal Logo" width="80"/>

<br/>

⭐ **Star this repo if you find it useful!** ⭐

</div>
