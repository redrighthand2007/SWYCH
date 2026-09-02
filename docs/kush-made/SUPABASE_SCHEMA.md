# ðŸ—„ï¸ Postgres Database Schema

This document describes the Cloud Postgres data model for Swych.

## Collections Overview

```
Postgres/
â”œâ”€â”€ users/{uid}                    # User profiles
â”œâ”€â”€ items/{itemId}                 # Item listings
â”‚   â””â”€â”€ offers/{offerId}           # Offers on an item (subcollection)
â””â”€â”€ deals/{dealId}                 # Locked/completed deals
```

## ðŸ‘¤ Users Collection

**Path:** `users/{uid}`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `phone` | `string` | âœ… | Verified phone number (from Supabase Auth) |
| `name` | `string` | âœ… | Display name |
| `block` | `string` | âœ… | Hostel/block (from fixed dropdown) |
| `greenDots` | `number` | âœ… | Count of successfully completed deals |
| `redDots` | `number` | âœ… | Count of missed completions (as buyer) |
| `createdAt` | `timestamp` | âœ… | Account creation time |

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

## ðŸ“¦ Items Collection

**Path:** `items/{itemId}`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `sellerId` | `string` | âœ… | Reference to `users/{uid}` |
| `category` | `string` | âœ… | One of the 8 fixed categories |
| `title` | `string` | âœ… | Item title |
| `description` | `string` | âœ… | Item description |
| `price` | `number` | âœ… | Asking price (INR) |
| `photoUrl` | `string` | âœ… | Supabase Storage download URL |
| `status` | `string` | âœ… | `open` / `locked` / `sold` |
| `createdAt` | `timestamp` | âœ… | Listing creation time |

**Status Transitions:**
```
  open â”€â”€(seller accepts offer)â”€â”€â–º locked â”€â”€(both complete)â”€â”€â–º sold
   â–²                                  â”‚
   â””â”€â”€â”€â”€â”€â”€â”€(seller re-lists)â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
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

## ðŸ’° Offers Subcollection

**Path:** `items/{itemId}/offers/{offerId}`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `buyerId` | `string` | âœ… | Reference to `users/{uid}` |
| `amount` | `number` | âœ… | Cash offer amount (INR) |
| `note` | `string` | âŒ | Optional short message |
| `status` | `string` | âœ… | `pending` / `accepted` / `rejected` |
| `createdAt` | `timestamp` | âœ… | Offer submission time |

**Status Transitions:**
```
  pending â”€â”€(seller accepts THIS offer)â”€â”€â–º accepted
  pending â”€â”€(seller accepts OTHER offer)â”€â”€â–º rejected
```

## ðŸ¤ Deals Collection

**Path:** `deals/{dealId}`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `itemId` | `string` | âœ… | Reference to `items/{itemId}` |
| `sellerId` | `string` | âœ… | Reference to `users/{uid}` |
| `buyerId` | `string` | âœ… | Reference to `users/{uid}` |
| `finalPrice` | `number` | âœ… | Accepted offer amount |
| `lockedAt` | `timestamp` | âœ… | When seller accepted the offer |
| `completionDeadline` | `timestamp` | âœ… | `lockedAt + 3 days` |
| `status` | `string` | âœ… | `locked` / `completed` / `expired` |
| `completedAt` | `timestamp` | âŒ | When both parties confirmed completion |

**Status Transitions:**
```
  locked â”€â”€(both mark complete within 3 days)â”€â”€â–º completed
  locked â”€â”€(seller re-lists after deadline)â”€â”€â”€â”€â–º expired
```

## ðŸ” Common Queries

| Query | Collection | Filters | Order |
|-------|-----------|---------|-------|
| Browse by category | `items` | `category == X`, `status == "open"` | `createdAt DESC` |
| Near me items | `items` | `category == X`, `status == "open"`, `sellerBlock IN [nearby]` | `createdAt DESC` |
| My listings | `items` | `sellerId == currentUser` | `createdAt DESC` |
| Offers for item | `items/{id}/offers` | (all) | `amount DESC` |
| My deals (buyer) | `deals` | `buyerId == currentUser` | `lockedAt DESC` |
| My deals (seller) | `deals` | `sellerId == currentUser` | `lockedAt DESC` |
| Expired deals | `deals` | `status == "locked"`, `completionDeadline < now` | â€” |

## ðŸ“ Indexes Required

Postgres requires composite indexes for queries with multiple filters:

| Collection | Fields | Order |
|-----------|--------|-------|
| `items` | `category`, `status`, `createdAt` | ASC, ASC, DESC |
| `items` | `sellerId`, `createdAt` | ASC, DESC |
| `deals` | `buyerId`, `status` | ASC, ASC |
| `deals` | `sellerId`, `status` | ASC, ASC |

