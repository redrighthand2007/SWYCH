<div align="center">

<img src="docs/assets/logo.png" alt="Swych Logo" width="120" style="border-radius: 28px;" />

# Swych (Swap n' Switch)

A campus marketplace for VIT students: list what you don't need, find what you do,
and hand it over on campus.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white&labelColor=7F52FF)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white&labelColor=4285F4)](https://developer.android.com/jetpack/compose)
[![Supabase](https://img.shields.io/badge/Supabase-PostgreSQL-3ECF8E?logo=supabase&logoColor=white&labelColor=3ECF8E)](https://supabase.com/)
[![API Level](https://img.shields.io/badge/Min_SDK-26%20(Android_8.0)-4C1?logo=android&logoColor=white&labelColor=4c1)](https://developer.android.com/studio/releases/platforms)

</div>

## ✨ Features

- **Browse by category**: 24 student categories, filtered by campus or your hostel block, sorted by newest or price.
- **List in seconds**: take a photo (or pick one from the gallery), set a price, done. Photos are resized before upload.
- **Offers, not DMs**: offer the listed price with one tap, or make your own offer from the item page.
- **Safe hand-overs**: an accepted deal reserves the item; both sides confirm with **Done** after meeting up.
- **Phone numbers stay private**: the other person's number is only revealed once a deal is accepted.
- **Reputation**: green dots for completed deals, red dots for deals you cancelled.
- **Notifications**: new offers, accepted/rejected offers and deal updates, even when the app is closed.
- **Your data, your call**: edit or remove listings, and delete your account from Profile.
- **Light / dark / system theme** and haptic feedback, both switchable in Profile.

## 🔄 How a deal works

```text
Buyer offers ──► PENDING ──► seller Accepts ──► ACCEPTED ──► both tap Done ──► COMPLETED
                    │                              │   (item reserved)          (item sold)
                    ├─ seller Rejects ─► REJECTED  ├─ either side Cancels ─► CANCELLED (item back on sale)
                    └─ buyer Withdraws (removed)   └─ nobody confirms in 3 days ─► EXPIRED (item back on sale)
```

- Accepting one offer automatically rejects the other offers on that item.
- A buyer can have only one live offer per item; offers on reserved or sold items are refused.
- Every status change runs in a Supabase database function, in one transaction, so a deal can't end up half-updated.
- Expired deals are closed by an hourly scheduled job.

## 🛠️ Tech Stack

- **App:** Kotlin, Jetpack Compose, Material 3, Navigation Compose (type-safe routes), Coil, WorkManager
- **Backend:** Supabase — Auth, PostgREST, Storage, `pg_cron`
- **Database:** PostgreSQL with Row Level Security; deal logic in `SECURITY DEFINER` functions

## 📁 Project Structure

```text
SWYCH/
├── app/src/main/java/com/kush/swych/
│   ├── core/
│   │   ├── data/              # Repositories: auth, items, deals
│   │   ├── designsystem/      # Theme, colors, typography, shared components
│   │   ├── model/             # User, Item, Deal, Category + status constants
│   │   ├── network/           # Supabase client
│   │   ├── notifications/     # Deal notifications and background check
│   │   └── util/              # Settings, errors, images, haptics
│   └── ui/
│       ├── auth/              # Welcome, sign-up, login
│       ├── home/              # Category grid
│       ├── browse/            # Marketplace feed
│       ├── itemdetail/        # Item page, offers, edit/remove own listing
│       ├── postitem/          # New listing
│       ├── deals/             # Offers and My Products
│       ├── profile/           # Profile, settings, logout, delete account
│       ├── main/              # Tabs and bottom bar
│       └── navigation/        # Routes and session-based navigation
├── supabase/migrations/       # Database schema, functions and access rules
└── docs/                      # Logo assets and privacy policy
```

## ⚙️ Getting Started

1. **Clone**
   ```bash
   git clone https://github.com/redrighthand2007/SWYCH.git
   ```
2. **Open** the folder in Android Studio and let Gradle sync.
3. **Run** on an emulator or device with Android 8.0 (API 26) or newer.

The app points at the project's Supabase backend out of the box. Sign-up only accepts
`@vitstudent.ac.in` email addresses.

## 🔧 Using your own Supabase project

1. Create a [Supabase](https://supabase.com/) project with the `users`, `items` and `deals` tables
   and a public storage bucket named `items`.
2. Apply the SQL files in [`supabase/migrations/`](supabase/migrations) in order. They add the deal
   functions, constraints, sign-up trigger, access rules and the hourly expiry job.
3. Put your project URL and publishable key in
   [`core/network/SupabaseManager.kt`](app/src/main/java/com/kush/swych/core/network/SupabaseManager.kt).
4. In Supabase → Authentication → Settings, turn on **Leaked password protection**.

## 📦 Release build

The release build is shrunk with R8. To sign it with your own key, create `keystore.properties`
in the project root (it is git-ignored):

```properties
storeFile=../swych-release.jks
storePassword=...
keyAlias=swych
keyPassword=...
```

Without that file the release build is signed with the debug key, which is fine for testing but
not for the Play Store. Then run:

```bash
./gradlew bundleRelease
```

Before publishing, host the [privacy policy](docs/PRIVACY_POLICY.md) at a public URL and add it to
the Play Console listing.

## 🧪 Tests

```bash
./gradlew testDebugUnitTest lintDebug
```

## 🗺️ Roadmap

- [x] Sign-up / login (email or phone)
- [x] Listings with photos, edit and remove
- [x] Offers, accept/reject, hand-over confirmation, expiry
- [x] Reputation (completed vs cancelled)
- [x] Row Level Security and server-side deal logic
- [x] Notifications (background check)
- [x] Account deletion
- [ ] Instant push notifications (Firebase Cloud Messaging)
- [ ] In-app chat
- [ ] Free / donate listings and a "Wanted" board
- [ ] Report and block, admin moderation

## 📄 License

Distributed under the MIT License. See [`LICENSE`](LICENSE).

## 👨‍💻 Author

**Kush** · GitHub: [@redrighthand2007](https://github.com/redrighthand2007)

## 🙌 Acknowledgements

- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern Android UI
- [Supabase](https://supabase.com/) for auth, database and storage
- [Icons8](https://icons8.com/) for category iconography
- [Shields.io](https://shields.io/) for the repository badges
