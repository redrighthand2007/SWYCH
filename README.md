<div align="center">

<img src="app/src/main/res/drawable/logo.png" alt="Swych Logo" width="120" style="border-radius: 28px;" />

# Swych

### The campus marketplace that actually works.

**Buy. Sell. Deal. On Campus.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Supabase](https://img.shields.io/badge/Supabase-PostgreSQL-3ECF8E?logo=supabase&logoColor=white)](https://supabase.com/)
[![API Level](https://img.shields.io/badge/Min_SDK-26%20(Android_8.0)-brightgreen)](https://developer.android.com/studio/releases/platforms)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

</div>

---

## ?? Project Overview

### What is it?

Swych is a **peer-to-peer campus trading app** built for college students. It lets you list items you no longer need, discover what others are selling nearby, make offers, and lock in deals — all from your phone, all within your campus community.

No middlemen. No shipping fees. Just students helping students.

### Why did you build it?
Existing campus trading happens in chaotic, fragmented WhatsApp or Telegram groups where listings get buried, negotiations are disorganized, and buyers frequently "ghost" on meetups.

---

## ?? Screenshots & Demo

**?? Demo � Coming Soon!**


> *Coming soon — run the app to see it in action!*

---

### What does it do?

| Feature | Status |
|---------|--------|
| Browse campus listings by category | ✅ Live |
| List an item with photo | ✅ Live |
| Make offers on items | ✅ Live |
| Lock a deal & track status | ✅ Live |
| My Listings dashboard | ✅ Live |
| My Deals tracker | ✅ Live |
| Shimmer skeleton loading | ✅ Live |
| User profile with reputation dots | ✅ Live |
| Register & Login | ✅ Live |
| Cloudinary image uploads | 🔧 In Progress |
| Push notifications | 📋 Planned |
| Campus Beta Launch | 📋 Planned |

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Kotlin 2.0 |
| **UI** | Jetpack Compose + Material 3 |
| **Navigation** | Compose Navigation (Type-safe Routes) |
| **Database** | Supabase (PostgreSQL) |
| **Auth** | Supabase Auth (via SharedPreferences session) |
| **Image Loading** | Coil 3 + OkHttp |
| **Image Storage** | Cloudinary |
| **Serialization** | Kotlinx Serialization |
| **Architecture** | Cloud-First, MVVM-inspired |

---

### How does it work? (Architecture)

Swych uses a **Cloud-First** approach — no local database. All data lives in Supabase PostgreSQL and is fetched on demand. Screens show shimmer skeleton loaders while data loads, giving a fast, modern feel.

```
UI Layer (Compose Screens)
        ↓
Repository Layer (Suspend functions → Result<T>)
        ↓
Supabase Client (PostgREST API)
        ↓
PostgreSQL Database (Supabase Cloud)
```

**Key design decisions:**
- No Room DB — keeps the app simple and data always fresh
- `Result<T>` return types for clean error handling
- `LaunchedEffect` data fetching with `mutableStateOf<T?>(null)` shimmer pattern
- Type-safe navigation routes using `@Serializable` data classes

---

## Database Schema

<details>
<summary><b>PostgreSQL Tables (click to expand)</b></summary>

### `users`
| Column | Type | Description |
|--------|------|-------------|
| `uid` | TEXT (PK) | Unique user ID |
| `name` | TEXT | Display name |
| `block` | TEXT | Hostel block |
| `phone` | TEXT | Phone number |
| `email` | TEXT | Email address |
| `green_dots` | INTEGER | Completed deals (reputation) |
| `red_dots` | INTEGER | Missed deals (reputation) |

### `items`
| Column | Type | Description |
|--------|------|-------------|
| `id` | TEXT (PK) | Unique item ID |
| `seller_id` | TEXT (FK) | References `users.uid` |
| `category` | TEXT | Item category |
| `title` | TEXT | Item title |
| `description` | TEXT | Item description |
| `price` | REAL | Asking price (₹) |
| `photo_url` | TEXT | Cloudinary image URL |
| `status` | TEXT | `OPEN` / `LOCKED` / `SOLD` |

### `deals`
| Column | Type | Description |
|--------|------|-------------|
| `id` | TEXT (PK) | Unique deal ID |
| `item_id` | TEXT (FK) | References `items.id` |
| `buyer_id` | TEXT (FK) | References `users.uid` |
| `seller_id` | TEXT (FK) | References `users.uid` |
| `item_title` | TEXT | Snapshot of item name |
| `item_photo_url` | TEXT | Snapshot of item photo |
| `final_price` | REAL | Agreed deal price |
| `status` | TEXT | `LOCKED` / `COMPLETED` / `EXPIRED` |
| `timestamp` | BIGINT | Deal creation time |

</details>

---

## Project Structure

```
app/src/main/java/com/kush/swych/
├── MainActivity.kt
├── core/
│   ├── data/              # Repositories (AuthRepository, ItemRepository, DealRepository)
│   ├── designsystem/
│   │   ├── component/     # ItemCard, DotBadge, CategoryChip, ShimmerEffect, DealButton
│   │   └── theme/         # Colors, Typography, Shapes, Material 3 Theme
│   ├── model/             # User, Item, Deal, Category (all @Serializable)
│   ├── network/           # SupabaseManager, CloudinaryManager
│   └── util/              # Constants, Resource, SettingsManager
└── ui/
    ├── auth/              # AuthScreen, LoginScreen, SignUpScreen
    ├── browse/            # BrowseScreen (shimmer grid of items)
    ├── deals/             # DealsScreen (live deal tracker)
    ├── home/              # HomeScreen (bottom nav host)
    ├── itemdetail/        # ItemDetailScreen (parallax + offer form)
    ├── main/              # MainScreen (tab switcher)
    ├── mylistings/        # MyListingsScreen (seller dashboard)
    ├── navigation/        # AppNavHost, Routes
    ├── offers/            # OffersScreen
    ├── postitem/          # PostItemScreen (image + form)
    └── profile/           # ProfileScreen (user + reputation dots)
```

---

## Getting Started

### Prerequisites

- Android Studio **Hedgehog** (2023.1.1) or newer
- JDK 17+
- Android device or emulator running **Android 8.0 (API 26)+**
- A [Supabase](https://supabase.com) project

### Setup

```bash
# 1. Clone the repo
git clone https://github.com/redrighthand2007/Swych-App.git
cd Swych-App
```

**2. Set up Supabase:**
- Go to your [Supabase dashboard](https://supabase.com/dashboard)
- Open the **SQL Editor** and run the schema from `docs/SUPABASE_SCHEMA.md`
- Copy your **Project URL** and **Anon Key**

**3. Add your credentials:**

Open `app/src/main/java/com/kush/swych/core/network/SupabaseManager.kt` and update:

```kotlin
private const val SUPABASE_URL = "your-project-url"
private const val SUPABASE_ANON_KEY = "your-anon-key"
```

**4. Run the app:**
- Open the project in Android Studio
- Click **Run ▶️**

---

## Categories

Swych is tuned for campus life. Current supported categories:

- 🍱 **Eatables** — food, snacks, mess coupons
- 👕 **Wearables** — clothes, shoes, accessories
- 🚲 **Cycles** — bicycles, MTBs
- 🧮 **Calculators** — scientific, graphing
- 🥼 **Lab Coats** — lab essentials
- 📱 **Subscription Plans** — Netflix, Spotify splits
- 📚 **Study Notes** — handwritten, printed
- 🎮 **Game Accounts** — BGMI, VALORANT, etc.

---

## How Deals Work

1. **Seller lists an item** → fills category, title, description, price, photo
2. **Buyer browses** → taps item, sees full detail screen
3. **Buyer makes an offer** → enters their price
4. **Deal is created** → item status flips to `LOCKED`
5. **Both parties meet on campus** → seller marks deal complete
6. **Reputation updates** → green dot for success, red dot for a no-show

---

## Reputation System

Every user has two counters visible on their profile:

- 🟢 **Green Dots** — number of successfully completed deals
- 🔴 **Red Dots** — number of deals they backed out of

This gives every buyer and seller an instant trust signal before committing to a deal.

---

## Contributing

Contributions are welcome! Here's how to get involved:

1. Fork the repo
2. Create a feature branch — `git checkout -b add-push-notifications`
3. Make your changes
4. Open a Pull Request with a short description

See [CONTRIBUTING.md](CONTRIBUTING.md) for full guidelines.

---

## Roadmap

- [ ] Cloudinary image uploads (in progress)
- [ ] Real OTP-based phone authentication
- [ ] Push notifications for new offers
- [ ] Chat between buyer and seller
- [ ] Campus-specific filtering (block-based proximity)
- [ ] Admin panel for moderation
- [ ] Android 15 edge-to-edge support

---

## License

Distributed under the **MIT License** — see [LICENSE](LICENSE) for more information.

---

<div align="center">

Built with ❤️ for campus students everywhere.

⭐ **Star this repo if Swych helped you!** ⭐

</div>


