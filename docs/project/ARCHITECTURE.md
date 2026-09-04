# 🏗️ Architecture Overview

## High-Level Architecture

Swych follows **MVVM + Clean Architecture** principles, ensuring separation of concerns, testability, and scalability.

```
┌─────────────────────────────────────────────────────┐
│                    UI LAYER                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐   │
│  │ Screens  │  │ViewModels│  │  UI State/Events │   │
│  │(Compose) │◄─┤(StateFlow│──┤ (Sealed Classes) │   │
│  └──────────┘  └────┬─────┘  └──────────────────┘   │
│                     │                                │
├─────────────────────┼────────────────────────────────┤
│               DOMAIN LAYER                           │
│  ┌──────────────────┴──────────────────────┐         │
│  │              Use Cases                   │         │
│  │  PostItemUseCase  │  AcceptOfferUseCase  │         │
│  │  SendOtpUseCase   │  MarkCompleteUseCase │         │
│  └──────────────────┬──────────────────────┘         │
│  ┌──────────────────┴──────────────────────┐         │
│  │         Repository Interfaces            │         │
│  └──────────────────┬──────────────────────┘         │
├─────────────────────┼────────────────────────────────┤
│                DATA LAYER                            │
│  ┌──────────────────┴──────────────────────┐         │
│  │        Repository Implementations        │         │
│  └──────────────────┬──────────────────────┘         │
│  ┌──────────┐ ┌─────┴──────┐ ┌───────────────┐      │
│  │ Supabase │ │ Postgres  │ │   Supabase    │      │
│  │   Auth   │ │  Database  │ │   Cloudinary     │      │
│  └──────────┘ └────────────┘ └───────────────┘      │
└─────────────────────────────────────────────────────┘
```

## Layer Details

### UI Layer (`ui/`)

Each feature is organized into its own package containing:

| File | Purpose |
|------|--------|
| `XxxScreen.kt` | Composable UI — renders state, dispatches events |
| `XxxViewModel.kt` | Holds UI state as `StateFlow`, processes events |
| `XxxUiState.kt` | Sealed interface defining all possible screen states |

**Pattern:** Unidirectional Data Flow (UDF)
```
User Action → ViewModel.onEvent() → UseCase → Repository → Postgres
                    ↓
              StateFlow<UiState>
                    ↓
              Composable recomposes
```

### Domain Layer (`domain/`)

- **Use Cases**: Single-responsibility classes encapsulating business logic
- **Repository Interfaces**: Contracts that the data layer must fulfill
- **No framework dependencies** — pure Kotlin

### Data Layer (`data/`)

- **Repository Implementations**: Concrete implementations using Supabase SDKs
- **Data Sources**: Direct Supabase API wrappers
- **Model Mapping**: Postgres documents ↔ domain models

### Core (`core/`)

- **Design System**: Theme, colors, typography, shapes, reusable composables
- **DI Modules**: Hilt modules providing Supabase instances and bindings
- **Models**: Shared data classes used across layers
- **Utilities**: Result wrappers, constants, extension functions

## Navigation Architecture

```
                    ┌─────────────┐
                    │   App Start  │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  Auth Check  │
                    └──────┬──────┘
                  ┌────────┼────────┐
            Not Logged In    │    Logged In
                  │          │         │
          ┌───────▼──┐  ┌───▼────┐   ┌▼──────────┐
          │Auth Screen│  │Profile │   │   Home    │
          │(Phone OTP)│  │ Setup  │   │  Screen   │
          └───────┬──┘  └───┬────┘   └─────┬─────┘
                  │         │               │
                  └─────────┘         ┌─────┼──────────┐
                                      │     │          │
                                ┌─────▼┐ ┌──▼────┐ ┌──▼──────┐
                                │ Post │ │ Item  │ │   My    │
                                │ Item │ │Detail │ │Listings │
                                └──────┘ └───┬───┘ └────┬────┘
                                             │          │
                                        ┌────▼────┐┌────▼────┐
                                        │  Submit ││ Offers  │
                                        │  Offer  ││ Review  │
                                        └─────────┘└────┬────┘
                                                        │
                                                   ┌────▼────┐
                                                   │  Deal   │
                                                   │ Screen  │
                                                   └─────────┘
```

## Data Flow: Accept Offer (Critical Path)

The most complex operation — accepting an offer — uses a Postgres **batch write** for atomicity:

```
Seller taps "Accept" on an offer
        │
        ▼
┌───────────────────────────────────┐
│     AcceptOfferUseCase            │
│                                   │
│  1. Batch write:                  │
│     • offers/{accepted} → status: "accepted"  │
│     • offers/{others}   → status: "rejected"  │
│     • items/{id}        → status: "locked"     │
│     • deals/{new}       → create with:         │
│         - finalPrice from accepted offer       │
│         - completionDeadline = now + 3 days    │
│         - status: "locked"                     │
│  2. Commit atomically                          │
└───────────────────────────────────┘
        │
        ▼
  Both users see WhatsApp deep link
  to arrange the handoff
```

## Reputation System Flow

```
 Deal Locked (3-day window)
        │
    ┌───┴───────────────────┐
    │                       │
 Both confirm              Buyer doesn't confirm
    │                       │
    ▼                       ▼
 +1 🟢 to BOTH         Seller taps "Re-list"
 users                     │
                           ▼
                    +1 🔴 to BUYER
                    Item → status: "open"
                    Deal → status: "expired"
```

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin 2.1+ |
| UI Framework | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt (Dagger) |
| Navigation | Compose Navigation 2.8+ (Type-safe) |
| Auth | Supabase Phone Auth (OTP) |
| Database | Cloud Postgres |
| Cloudinary | Supabase Cloudinary |
| Images | Coil 3 |
| Async | Coroutines + Flow |
| Serialization | kotlinx.serialization |
| CI/CD | GitHub Actions |


