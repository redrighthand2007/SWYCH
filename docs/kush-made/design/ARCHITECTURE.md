# ðŸ—ï¸ Architecture Overview

## High-Level Architecture

Swych follows **MVVM + Clean Architecture** principles, ensuring separation of concerns, testability, and scalability.

```
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚                    UI LAYER                          â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”   â”‚
â”‚  â”‚ Screens  â”‚  â”‚ViewModelsâ”‚  â”‚  UI State/Events â”‚   â”‚
â”‚  â”‚(Compose) â”‚â—„â”€â”¤(StateFlowâ”‚â”€â”€â”¤ (Sealed Classes) â”‚   â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜  â””â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”˜  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â”‚
â”‚                     â”‚                                â”‚
â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
â”‚               DOMAIN LAYER                           â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”         â”‚
â”‚  â”‚              Use Cases                   â”‚         â”‚
â”‚  â”‚  PostItemUseCase  â”‚  AcceptOfferUseCase  â”‚         â”‚
â”‚  â”‚  SendOtpUseCase   â”‚  MarkCompleteUseCase â”‚         â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜         â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”         â”‚
â”‚  â”‚         Repository Interfaces            â”‚         â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜         â”‚
â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
â”‚                DATA LAYER                            â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”         â”‚
â”‚  â”‚        Repository Implementations        â”‚         â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜         â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â” â”Œâ”€â”€â”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â” â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”      â”‚
â”‚  â”‚ Supabase â”‚ â”‚ Postgres  â”‚ â”‚   Supabase    â”‚      â”‚
â”‚  â”‚   Auth   â”‚ â”‚  Database  â”‚ â”‚   Cloudinary     â”‚      â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜ â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜ â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜      â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
```

## Layer Details

### UI Layer (`ui/`)

Each feature is organized into its own package containing:

| File | Purpose |
|------|--------|
| `XxxScreen.kt` | Composable UI â€” renders state, dispatches events |
| `XxxViewModel.kt` | Holds UI state as `StateFlow`, processes events |
| `XxxUiState.kt` | Sealed interface defining all possible screen states |

**Pattern:** Unidirectional Data Flow (UDF)
```
User Action â†’ ViewModel.onEvent() â†’ UseCase â†’ Repository â†’ Postgres
                    â†“
              StateFlow<UiState>
                    â†“
              Composable recomposes
```

### Domain Layer (`domain/`)

- **Use Cases**: Single-responsibility classes encapsulating business logic
- **Repository Interfaces**: Contracts that the data layer must fulfill
- **No framework dependencies** â€” pure Kotlin

### Data Layer (`data/`)

- **Repository Implementations**: Concrete implementations using Supabase SDKs
- **Data Sources**: Direct Supabase API wrappers
- **Model Mapping**: Postgres documents â†” domain models

### Core (`core/`)

- **Design System**: Theme, colors, typography, shapes, reusable composables
- **DI Modules**: Hilt modules providing Supabase instances and bindings
- **Models**: Shared data classes used across layers
- **Utilities**: Result wrappers, constants, extension functions

## Navigation Architecture

```
                    â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
                    â”‚   App Start  â”‚
                    â””â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”˜
                           â”‚
                    â”Œâ”€â”€â”€â”€â”€â”€â–¼â”€â”€â”€â”€â”€â”€â”
                    â”‚  Auth Check  â”‚
                    â””â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”˜
                  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”¼â”€â”€â”€â”€â”€â”€â”€â”€â”
            Not Logged In    â”‚    Logged In
                  â”‚          â”‚         â”‚
          â”Œâ”€â”€â”€â”€â”€â”€â”€â–¼â”€â”€â”  â”Œâ”€â”€â”€â–¼â”€â”€â”€â”€â”   â”Œâ–¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
          â”‚Auth Screenâ”‚  â”‚Profile â”‚   â”‚   Home    â”‚
          â”‚(Phone OTP)â”‚  â”‚ Setup  â”‚   â”‚  Screen   â”‚
          â””â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”˜  â””â”€â”€â”€â”¬â”€â”€â”€â”€â”˜   â””â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”˜
                  â”‚         â”‚               â”‚
                  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜         â”Œâ”€â”€â”€â”€â”€â”¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
                                      â”‚     â”‚          â”‚
                                â”Œâ”€â”€â”€â”€â”€â–¼â” â”Œâ”€â”€â–¼â”€â”€â”€â”€â” â”Œâ”€â”€â–¼â”€â”€â”€â”€â”€â”€â”
                                â”‚ Post â”‚ â”‚ Item  â”‚ â”‚   My    â”‚
                                â”‚ Item â”‚ â”‚Detail â”‚ â”‚Listings â”‚
                                â””â”€â”€â”€â”€â”€â”€â”˜ â””â”€â”€â”€â”¬â”€â”€â”€â”˜ â””â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”˜
                                             â”‚          â”‚
                                        â”Œâ”€â”€â”€â”€â–¼â”€â”€â”€â”€â”â”Œâ”€â”€â”€â”€â–¼â”€â”€â”€â”€â”
                                        â”‚  Submit â”‚â”‚ Offers  â”‚
                                        â”‚  Offer  â”‚â”‚ Review  â”‚
                                        â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜â””â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”˜
                                                        â”‚
                                                   â”Œâ”€â”€â”€â”€â–¼â”€â”€â”€â”€â”
                                                   â”‚  Deal   â”‚
                                                   â”‚ Screen  â”‚
                                                   â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
```

## Data Flow: Accept Offer (Critical Path)

The most complex operation â€” accepting an offer â€” uses a Postgres **batch write** for atomicity:

```
Seller taps "Accept" on an offer
        â”‚
        â–¼
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚     AcceptOfferUseCase            â”‚
â”‚                                   â”‚
â”‚  1. Batch write:                  â”‚
â”‚     â€¢ offers/{accepted} â†’ status: "accepted"  â”‚
â”‚     â€¢ offers/{others}   â†’ status: "rejected"  â”‚
â”‚     â€¢ items/{id}        â†’ status: "locked"     â”‚
â”‚     â€¢ deals/{new}       â†’ create with:         â”‚
â”‚         - finalPrice from accepted offer       â”‚
â”‚         - completionDeadline = now + 3 days    â”‚
â”‚         - status: "locked"                     â”‚
â”‚  2. Commit atomically                          â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
        â”‚
        â–¼
  Both users see WhatsApp deep link
  to arrange the handoff
```

## Reputation System Flow

```
 Deal Locked (3-day window)
        â”‚
    â”Œâ”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
    â”‚                       â”‚
 Both confirm              Buyer doesn't confirm
    â”‚                       â”‚
    â–¼                       â–¼
 +1 ðŸŸ¢ to BOTH         Seller taps "Re-list"
 users                     â”‚
                           â–¼
                    +1 ðŸ”´ to BUYER
                    Item â†’ status: "open"
                    Deal â†’ status: "expired"
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


