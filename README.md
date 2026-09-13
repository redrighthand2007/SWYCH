<div align="center">

<img src="docs/assets/logo.png" alt="Swych Logo" width="120" style="border-radius: 28px;" />

# Swych (Swap n' Switch)

The campus marketplace that actually works. Buy. Sell. Deal. On Campus.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white&labelColor=7F52FF)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white&labelColor=4285F4)](https://developer.android.com/jetpack/compose)
[![Supabase](https://img.shields.io/badge/Supabase-PostgreSQL-3ECF8E?logo=supabase&logoColor=white&labelColor=3ECF8E)](https://supabase.com/)
[![API Level](https://img.shields.io/badge/Min_SDK-26%20(Android_8.0)-4C1?logo=android&logoColor=white&labelColor=4c1)](https://developer.android.com/studio/releases/platforms)

</div>

## 📸 Preview / Demo

> *Demo / Screenshots Coming Soon!*

## ✨ Features

- **Campus-Specific Browsing**: Filter items by category, location (Hostel/Campus), and sort by newest or price.
- **Instant Listing**: Take a photo and post an item for sale in seconds.
- **Deal Negotiation**: Make offers directly on items instead of sliding into messy DMs.
- **Deal Management Dashboards**: Track the status of your offers and active listings as Pending, Sold, or Rejected.
- **Integrated Contacting**: Once a deal is struck, instantly reveal the buyer/seller phone number to connect.
- **User Profiles**: Track users by name, hostel block, and reputation dots (Deals Made / Deals Expired).
- **Edge-to-Edge Design**: Beautiful UI with OLED dark mode support and floating, pill-shaped navigation bars.

## 🎯 Why This Project?

Existing campus trading happens in chaotic, fragmented WhatsApp or Telegram groups where listings get buried, negotiations are disorganized, and buyers frequently "ghost" on meetups.

**Swych solves this by providing:**
- A structured, visual marketplace.
- A streamlined "First-Come-First-Serve" locking mechanism.
- Accountability through profiles tracking completed vs expired deals.

## 🛠️ Tech Stack

- **Frontend:** Android (Kotlin, Jetpack Compose, Material 3, Coil)
- **Backend:** Supabase (Auth, PostgREST API)
- **Database:** PostgreSQL (Supabase DB with Row Level Security)
- **Storage:** Cloudinary / Supabase Storage (for fast, optimized image hosting)

## 📁 Project Structure

```text
SWYCH/
├── app/src/main/java/com/kush/swych/
│   ├── core/                  # Core logic, network, and data models
│   │   ├── data/              # Repositories (Auth, Deals, Items)
│   │   ├── designsystem/      # Theme, Colors, Typography, Common UI Components
│   │   ├── model/             # Data models (User, Item, Deal)
│   │   └── network/           # Supabase client config
│   ├── ui/                    # UI screens and navigation
│   │   ├── auth/              # Login and Sign-up screens
│   │   ├── browse/            # Marketplace feed
│   │   ├── deals/             # User's offers and listings management
│   │   ├── postitem/          # Item creation flow
│   │   └── profile/           # User dashboard
└── README.md
```

## ⚙️ Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/redrighthand2007/CacheDeal-App.git
   cd SWYCH
   ```
2. **Open the project:**
   Open the folder in **Android Studio** (Koala or newer recommended).
3. **Sync Gradle:**
   Allow Android Studio to sync the Gradle files and download all required dependencies.

## 🚀 Usage

1. Build and run the app on an Android Emulator or physical device (API 26+).
2. Create an account.
3. Browse the marketplace or post an item to start trading!

## 🔧 Configuration

To run your own backend, you must configure Supabase:

1. Create a [Supabase](https://supabase.com/) project.
2. In your Android Studio project, locate your Supabase initialization (inside `core/network/SupabaseManager.kt`).
3. Replace the placeholder Supabase URL and Anon Key with your actual project credentials.
4. Run the necessary SQL migrations to create the `users`, `items`, and `deals` tables.

## 📊 Results / Performance

> *Performance metrics and benchmarks coming soon.*

## 🧠 How It Works

- **Architecture**: The app follows the **MVVM (Model-View-ViewModel)** architecture and uses Jetpack Compose for declarative UI. State is managed via `StateFlow` and Coroutines for asynchronous operations.
- **Deal Flow Logic**: Swych uses a **First-Come-First-Serve** system. When a buyer submits an offer, the deal status locks to **PENDING**. The seller can then either **Accept** (marks as SOLD, hides from feed, reveals contact) or **Reject** (cancels deal, item returns to active pool).

## 🗺️ Roadmap

- [x] Basic Authentication (Sign up / Login)
- [x] Post items with images
- [x] Browse marketplace and filter
- [x] Make offers and manage deals
- [ ] Push Notifications for new offers
- [ ] In-app messaging system
- [ ] iOS version using Kotlin Multiplatform

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!
Feel free to check the [issues page](https://github.com/redrighthand2007/CacheDeal-App/issues) if you want to contribute.

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

## 👨‍💻 Author

**Kush**
- GitHub: [@redrighthand2007](https://github.com/redrighthand2007)

## ⭐ Acknowledgements

- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern Android UI
- [Supabase](https://supabase.com/) for a seamless open-source Firebase alternative
- [Shields.io](https://shields.io/) for the clean repository badges
