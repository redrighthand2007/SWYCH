<div align="center">

<img src="docs/assets/logo.png" alt="Swych Logo" width="120" style="border-radius: 28px;" />

# Swap n' Switch

### The campus marketplace that actually works.

**Buy. Sell. Deal. On Campus.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Supabase](https://img.shields.io/badge/Supabase-PostgreSQL-3ECF8E?logo=supabase&logoColor=white)](https://supabase.com/)
[![API Level](https://img.shields.io/badge/Min_SDK-26%20(Android_8.0)-brightgreen)](https://developer.android.com/studio/releases/platforms)

</div>

---

## 📖 Project Overview

### What is it?

Swych is a **peer-to-peer campus trading app** built for college students. It lets you list items you no longer need, discover what others are selling nearby, make offers, and lock in deals — all from your phone, all within your campus community.

No middlemen. No shipping fees. Just students helping students.

### Why did you build it?
Existing campus trading happens in chaotic, fragmented WhatsApp or Telegram groups where listings get buried, negotiations are disorganized, and buyers frequently "ghost" on meetups.

---

## 📸 Screenshots & Demo

**▶️ Demo — Coming Soon!**


> *Coming soon — run the app to see it in action!*

---

## 🚀 Features & Status

### What does it do?

| Feature | Status |
| :--- | :--- |
| **Browse campus listings by category** | ✅ Live |
| **List an item with photo** | ✅ Live |
| **Make offers on items** | ✅ Live |
| **Lock a deal & track status** | ✅ Live |
| **My Listings dashboard** | ✅ Live |
| **My Deals tracker** | ✅ Live |
| **User Profiles (Block/Hostel tracking)** | ✅ Live |
| **Dark Mode / Light Mode Support** | ✅ Live |

---

## 🛠️ Tech Stack & Architecture

Swych is built entirely with modern Android development standards.

### Client-Side (Android)
* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material 3 Design)
* **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
* **Image Loading:** Coil
* **Navigation:** Jetpack Navigation Compose
* **Asynchronous Logic:** Kotlin Coroutines & Flows

### Backend-as-a-Service (Supabase)
* **Database:** PostgreSQL (Supabase DB)
* **Authentication:** Supabase Auth (Email/Password)
* **Storage:** Cloudinary (for fast, optimized image hosting) & Supabase Storage
* **API:** PostgREST via Supabase Kotlin Client

### System Architecture
### Architecture Overview (MVVM + Clean Architecture)
* **UI Layer:** Jetpack Compose, ViewModels, StateFlow.
* **Domain Layer:** Use Cases (PostItemUseCase, AcceptOfferUseCase).
* **Data Layer:** SupabaseNetworkDataSource, DealRepository, ItemRepository.

### Database Schema (Supabase)
* **`users` table:** `uid`, `name`, `block`, `phone`, `email`.
* **`items` table:** `id`, `seller_id`, `title`, `description`, `price`, `category`, `status`, `photo_url`.
* **`deals` table:** `id`, `item_id`, `buyer_id`, `seller_id`, `status` (PENDING, SOLD, REJECTED).
*(Row Level Security (RLS) is enabled for all tables to protect user data).*

### Deal Flow Logic (First-Come-First-Serve)
When a buyer submits an offer, the item and deal status are instantly locked to **PENDING**. The seller can then either **Accept** (marks as SOLD and hides from feed) or **Reject** (returns item to OPEN).

---

<div align="center">
Made with ❤️ for campus communities.
</div>
