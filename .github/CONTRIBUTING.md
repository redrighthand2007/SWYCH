# Contributing to Swych

Thanks for your interest in contributing! Swych is a campus-first project and every contribution helps make it better for students.

---

## Quick Start

1. **Fork** the repository
2. **Clone** your fork — `git clone https://github.com/YOUR_USERNAME/Swych-App.git`
3. **Create a branch** — `git checkout -b add-push-notifications`
4. **Make your changes**
5. **Push** — `git push origin add-push-notifications`
6. **Open a Pull Request**

---

## Development Setup

**You'll need:**
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android device or emulator running Android 8.0+ (API 26)
- A [Supabase](https://supabase.com) project

**Steps:**
1. Clone the repo and open in Android Studio
2. Go to `core/network/SupabaseManager.kt` and add your Supabase URL and Anon Key
3. Run the SQL from `docs/SUPABASE_SCHEMA.md` in your Supabase SQL editor
4. Hit **Run ▶️** in Android Studio

---

## What You Can Help With

- **Bug fixes** — spotted something broken? Open an issue or fix it directly
- **New features** — check the Roadmap in the README for ideas
- **UI improvements** — better layouts, animations, or dark mode tweaks
- **Documentation** — typos, better explanations, missing steps
- **Performance** — faster loading, better error handling

---

## Coding Standards

### Kotlin
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use clear, descriptive names for functions and variables
- Keep composables small and focused — one job each
- Use `data class` for all models
- Use `Result<T>` for async operations that can fail

### Architecture (Cloud-First)
- **No local database** — all data fetches directly from Supabase
- Screens use `LaunchedEffect` to fetch + `mutableStateOf<T?>(null)` for shimmer loading
- Repositories return `Result<T>` — never throw exceptions to the UI
- Keep business logic out of Composables

### Compose
- Use Material 3 components and theme tokens everywhere
- Shimmer loading for any screen that fetches remote data
- Use `remember {}` for expensive computations
- Avoid deeply nested Composables

---

## Commit Style

Keep it short and human. Max 4 words. No AI-style verbose messages.

```
add push notifications
fix login crash
update profile screen
tweak item card
```

---

## Pull Request Guidelines

- Keep PRs focused — one feature or fix per PR
- Add a short description of what you changed and why
- Test on a real device if possible
- Screenshots are appreciated for UI changes
- Don't open a PR for a major feature without creating an issue first

---

## Reporting Bugs

Open an issue and include:
- What you were doing
- What you expected to happen
- What actually happened
- Your Android version and device model

---

## Questions?

Open an issue with the `question` label — happy to help!

Thanks for contributing. 🙌
