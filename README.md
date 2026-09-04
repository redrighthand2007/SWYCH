<div align="center">

<img src="docs/assets/logo.png" alt="Swych Logo" width="120" style="border-radius: 28px;" />

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

## 📖 Project Overview

### What is it?

Swych is a **peer-to-peer campus trading app** built for college students. It lets you list items you no longer need, discover what others are selling nearby, make offers, and lock in deals — all from your phone, all within your campus community.

No middlemen. No shipping fees. Just students helping students.

### Why did you build it?
Existing campus trading happens in chaotic, fragmented WhatsApp or Telegram groups where listings get buried, negotiations are disorganized, and buyers frequently "ghost" on meetups.

---

## 📸 Screenshots & Demo

**🎥 Demo — Coming Soon!**


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
| **Push Notifications (Firebase)** | ⏳ Planned |
| **In-app Chat** | ⏳ Planned |

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
![Architecture Diagram](https://raw.githubusercontent.com/redrighthand2007/CacheDeal-App/main/docs/project/ARCHITECTURE.md) (See `docs/project/ARCHITECTURE.md` for full breakdown)

---

## 💻 Getting Started (For Developers)

Want to run Swych on your own machine? 

### Prerequisites
1. **Android Studio** (Koala or newer recommended)
2. **JDK 21+**
3. **Android SDK 35**

### Quick Setup

1. **Clone the repo:**
   ```bash
   git clone https://github.com/redrighthand2007/CacheDeal-App.git
   cd Swych-App
   ```

2. **Open the project in Android Studio.** Let Gradle sync (it might take a minute).

3. **Run the App:** 
   Hit the green "Play" button in Android Studio to install the app on your emulator or physical Android device.

*Note: The app points to a live production database hosted on Supabase, so you can immediately create an account and start testing!*

---

## 🤝 Contributing

We welcome contributions! Whether it's squashing bugs, suggesting new features, or improving the documentation, we'd love your help.

Please read our [Contributing Guidelines](.github/CONTRIBUTING.md) to get started. Don't forget to check our [Code of Conduct](.github/CODE_OF_CONDUCT.md).

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

<div align="center">
Made with ❤️ for campus communities.
</div>
