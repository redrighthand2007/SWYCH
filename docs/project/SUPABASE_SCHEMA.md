# 🗄️ Postgres Database Schema

This document describes the Cloud Postgres data model for Swych.

## Collections Overview

```
Postgres/
├── users/{uid}                    # User profiles
├── items/{itemId}                 # Item listings
│   └── offers/{offerId}           # Offers on an item (subcollection)
└── deals/{dealId}                 # Locked/completed deals
```

## 👤 Users Collection

**Path:** `users/{uid}`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `phone` | `string` | ✅ | Verified phone number (from Supabase Auth) |
| `name` | `string` | ✅ | Display name |
| `block` | `string` | ✅ | Hostel/block (from fixed dropdown) |
| `greenDots` | `number` | ✅ | Count of successfully completed deals |
| `redDots` | `number` | ✅ | Count of missed completions (as buyer) |
| `createdAt` | `timestamp` | ✅ | Account creation time |

**Example Document:**
```json
{
  "phone": "+919876543210",
  "name": "Rahul Sharma",
  "block": "Men's Hostel Q",
  "greenDots": 5,
  "redDots": 0,
  "createdAt": "2026-08-01T10:30:00Z"
}
```

## 📦 Items Collection

**Path:** `items/{itemId}`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `sellerId` | `string` | ✅ | Reference to `users/{uid}` |
| `category` | `string` | ✅ | One of the 8 fixed categories |
| `title` | `string` | ✅ | Item title |
| `description` | `string` | ✅ | Item description |
| `price` | `number` | ✅ | Asking price (INR) |
| `photoUrl` | `string` | ✅ | Supabase Storage download URL |
| `status` | `string` | ✅ | `open` / `locked` / `sold` |
| `createdAt` | `timestamp` | ✅ | Listing creation time |

**Status Transitions:**
```
  open ──(seller accepts offer)──► locked ──(both complete)──► sold
   ▲                                  │
   └───────(seller re-lists)──────────┘
```

**Categories (fixed v1 list):**
1. `Eatables` (packaged only)
2. `Wearables` (non-clothing)
3. `Cycles`
4. `Calculators`
5. `Lab Coats`
6. `Subscription Plans`
7. `Study Notes`
8. `Game Accounts`

## 💰 Offers Subcollection

**Path:** `items/{itemId}/offers/{offerId}`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `buyerId` | `string` | ✅ | Reference to `users/{uid}` |
| `amount` | `number` | ✅ | Cash offer amount (INR) |
| `note` | `string` | ❌ | Optional short message |
| `status` | `string` | ✅ | `pending` / `accepted` / `rejected` |
| `createdAt` | `timestamp` | ✅ | Offer submission time |

**Status Transitions:**
```
  pending ──(seller accepts THIS offer)──► accepted
  pending ──(seller accepts OTHER offer)──► rejected
```

## 🤝 Deals Collection

**Path:** `deals/{dealId}`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `itemId` | `string` | ✅ | Reference to `items/{itemId}` |
| `sellerId` | `string` | ✅ | Reference to `users/{uid}` |
| `buyerId` | `string` | ✅ | Reference to `users/{uid}` |
| `finalPrice` | `number` | ✅ | Accepted offer amount |
| `lockedAt` | `timestamp` | ✅ | When seller accepted the offer |
| `completionDeadline` | `timestamp` | ✅ | `lockedAt + 3 days` |
| `status` | `string` | ✅ | `locked` / `completed` / `expired` |
| `completedAt` | `timestamp` | ❌ | When both parties confirmed completion |

**Status Transitions:**
```
  locked ──(both mark complete within 3 days)──► completed
  locked ──(seller re-lists after deadline)────► expired
```

## 🔍 Common Queries

| Query | Collection | Filters | Order |
|-------|-----------|---------|-------|
| Browse by category | `items` | `category == X`, `status == "open"` | `createdAt DESC` |
| Near me items | `items` | `category == X`, `status == "open"`, `sellerBlock IN [nearby]` | `createdAt DESC` |
| My listings | `items` | `sellerId == currentUser` | `createdAt DESC` |
| Offers for item | `items/{id}/offers` | (all) | `amount DESC` |
| My deals (buyer) | `deals` | `buyerId == currentUser` | `lockedAt DESC` |
| My deals (seller) | `deals` | `sellerId == currentUser` | `lockedAt DESC` |
| Expired deals | `deals` | `status == "locked"`, `completionDeadline < now` | — |

## 📐 Indexes Required

Postgres requires composite indexes for queries with multiple filters:

| Collection | Fields | Order |
|-----------|--------|-------|
| `items` | `category`, `status`, `createdAt` | ASC, ASC, DESC |
| `items` | `sellerId`, `createdAt` | ASC, DESC |
| `deals` | `buyerId`, `status` | ASC, ASC |
| `deals` | `sellerId`, `status` | ASC, ASC |

