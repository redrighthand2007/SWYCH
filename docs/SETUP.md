# 🛠️ Development Setup Guide

Follow this guide to set up the CacheDeal project for local development.

## Prerequisites

| Requirement | Version |
|------------|--------|
| Android Studio | Ladybug (2024.2.1) or newer |
| JDK | 17+ |
| Android SDK | API 35 (Android 15) |
| Min SDK | API 26 (Android 8.0) |
| Kotlin | 2.1+ |
| Gradle | 8.x (bundled with project) |

## 📱 Step 1: Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/cachedeal.git
cd cachedeal
```

## 🔥 Step 2: Firebase Setup

### 2.1 Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"** → Name it `cachedeal`
3. Disable Google Analytics (optional for v1)
4. Click **Create project**

### 2.2 Register Android App

1. In Firebase Console → **Add app** → **Android**
2. Package name: `com.vit.cachedeal`
3. App nickname: `CacheDeal`
4. Debug signing certificate SHA-1:
   ```bash
   # From Android Studio terminal:
   ./gradlew signingReport
   ```
5. Download `google-services.json`
6. Place it in the `app/` directory

### 2.3 Enable Firebase Services

#### Phone Authentication
1. Firebase Console → **Authentication** → **Sign-in method**
2. Enable **Phone** provider
3. Add test phone numbers for development:
   - `+1 650-555-1234` → Code: `123456`
   - `+91 9999999999` → Code: `123456`

#### Cloud Firestore
1. Firebase Console → **Firestore Database** → **Create database**
2. Start in **test mode** (we'll add security rules later)
3. Choose a region close to your users (e.g., `asia-south1` for India)

#### Firebase Storage
1. Firebase Console → **Storage** → **Get started**
2. Start in **test mode**
3. Same region as Firestore

## 🏗️ Step 3: Build & Run

### Open in Android Studio
1. Open Android Studio
2. **File → Open** → Select the project directory
3. Wait for Gradle sync to complete

### Run on Emulator
1. Create an AVD: **Tools → Device Manager → Create Device**
   - Recommended: Pixel 7 / API 34
2. Click **Run ▶️** or press `Shift + F10`

### Run on Physical Device
1. Enable **Developer Options** and **USB Debugging** on your phone
2. Connect via USB
3. Select your device in the toolbar
4. Click **Run ▶️**

## 🧪 Step 4: Run Tests

```bash
# Unit tests
./gradlew testDebugUnitTest

# All tests
./gradlew test

# Lint check
./gradlew lintDebug
```

## 🔐 Step 5: Firestore Security Rules

Deploy security rules from the `firestore.rules` file:

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login and deploy
firebase login
firebase deploy --only firestore:rules
```

## 📂 Project Structure

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed architecture documentation.

## ❓ Troubleshooting

### "google-services.json not found"
- Ensure the file is in the `app/` directory (not the project root)
- Check that the package name in the JSON matches `com.vit.cachedeal`

### "Phone Auth not working on emulator"
- Add test phone numbers in Firebase Console
- Use the test phone number and verification code
- Ensure you've added the SHA-1 certificate

### "Gradle sync failed"
- File → Invalidate Caches and Restart
- Delete `.gradle/` and `build/` directories, then re-sync

### "Firestore permission denied"
- Check that your security rules allow the operation
- Verify the user is authenticated
- Check Firestore Console → Rules for any errors
