# CacheDeal — Product Requirements Document

**Platform:** Android (Kotlin, Jetpack Compose)
**Backend:** Firebase (Auth + Firestore + Storage)
**Status:** v1 scope locked, ready to build

---

## 1. Overview

A campus buy/sell app for VIT Vellore students to sell items they no longer need — cycles, calculators, lab coats, subscriptions, notes, game accounts — to other students nearby. Sellers list an item and a price; buyers make cash offers; the seller picks one; the two connect and settle the deal off-app.

**Problem it solves:** There's no structured way to discover these trades on campus today — it currently happens through word-of-mouth and scattered group chats, which is slow and easy to miss.

---

## 2. Goals & Non-Goals

**Goals (v1):**
- Let a seller list an item with a price and photo
- Let buyers browse by category and make cash offers
- Let the seller pick the best offer and lock the deal
- Get both sides to actually complete the handoff, with light accountability if they don't
- Keep verification lightweight but non-anonymous

**Explicitly out of scope for v1** (each was discussed and deliberately dropped — see Decision Log):
- No in-app payments or escrow
- No barter/item-swap offers
- No in-app chat/messaging
- No rentals — permanent sale only

---

## 3. Decision Log

| Topic | Alternatives considered | Final decision |
|---|---|---|
| Deal mechanism | (a) First-come-first-lock with an atomic Firestore transaction | **(b)** Multiple buyers submit offers, seller picks one — no race condition since the seller is the sole decision-maker |
| Barter | (a) Support cash + barter (buyer offers their own listed item) | **(b)** Cash-only for v1; barter dropped to reduce scope |
| Communication | (a) Full in-app chat, (b) optional note field on the offer | **(c)** No chat at all for v1; contact happens via WhatsApp deep link after the deal locks |
| Verification | (a) Restrict signup to @vitstudent.ac.in emails | **(b)** Any email allowed + phone number OTP + self-declared hostel/block, since email-domain restriction was rejected as too strict |
| Payment | (a) Build in-app payment/escrow flow | **(b)** No payment feature — cash or UPI handled face-to-face, off-app |
| Ghosting / accountability | (a) No mechanism, (b) manual "report user" button | **(c)** Automatic: seller's "re-list" action after a missed completion window raises a red dot; mutual completion raises a green dot for both |
| Deal finalization | (a) Require a separate buyer-confirms step after seller accepts | **(b)** Buyer's offer + seller's accept together count as mutual lock; no extra confirm step |
| Location/proximity | (a) Separate flows or sections for boys'/girls' hostels | **(b)** Single "hostel block" field on profile + a "near me" filter — no gender-specific logic needed |
| Rentals | (a) Support renting alongside selling | **(b)** Permanent sale only |
| Eatables | (a) Special shorter-window rules for perishables | **(b)** Not needed — eatables restricted to packaged goods only, so no different handling from other categories |
| Price sorting | (a) Global price sort across all listings | **(b)** Price sort only within a single item's offer list (seller comparing offers); browsing is filtered by category, not sorted by price |
| App scope | (a) Merge with the Skill-Barter app into one "Campus Exchange" | **(b)** Standalone app — schemas are close enough to merge later if wanted, but shipping two focused apps now is faster |

---

## 4. Categories (v1)

Fixed list, not free text:

1. Eatables (packaged only)
2. Wearables (non-clothing — watches, bags, accessories)
3. Cycles
4. Calculators
5. Lab coats
6. Subscription plans
7. Study notes
8. Game accounts (Steam, etc.)

More can be added later without a schema change — category is just a string field.

---

## 5. Data Schema

**`users/{uid}`**

| Field | Type | Notes |
|---|---|---|
| phone | string | verified via Firebase Phone Auth OTP |
| name | string | |
| block | string | hostel/block, picked from a fixed dropdown |
| greenDots | int | count of completed deals |
| redDots | int | count of missed completions |
| createdAt | timestamp | |

**`items/{id}`**

| Field | Type | Notes |
|---|---|---|
| sellerId | string | ref to users |
| category | string | one of the fixed categories |
| title | string | |
| description | string | |
| price | number | asking price |
| photoUrl | string | Firebase Storage URL |
| status | string | open / locked / sold |
| createdAt | timestamp | |

**`items/{id}/offers/{offerId}`**

| Field | Type | Notes |
|---|---|---|
| buyerId | string | ref to users |
| amount | number | cash offer |
| note | string, optional | short free text, e.g. "can pay by evening" |
| status | string | pending / accepted / rejected |
| createdAt | timestamp | |

**`deals/{id}`**

| Field | Type | Notes |
|---|---|---|
| itemId | string | |
| sellerId | string | |
| buyerId | string | |
| finalPrice | number | from the accepted offer |
| lockedAt | timestamp | when the seller accepted |
| completionDeadline | timestamp | lockedAt + 3 days |
| status | string | locked / completed / expired |
| completedAt | timestamp, nullable | |

---

## 6. Core User Flow

1. Seller posts an item (category, title, price, one photo)
2. Buyers browse by category, optionally filtered to "near me" (same/nearby block)
3. Interested buyers submit a cash offer with an optional note
4. Seller opens the offers list, sorted by amount, each offer showing the buyer's green/red dot record
5. Seller accepts one offer → single batch write: that offer → accepted, all others → rejected, item → locked
7. Both sides have 3 days to meet up and tap "Mark as completed"
8. Both confirm in time → item stays sold, both get +1 green dot
9. Buyer doesn't confirm in time → seller can re-list the item (status back to open) → buyer gets +1 red dot automatically

---

## 7. Feature List

**Auth & Profile**
- Phone number OTP signup
- Hostel/block selection at onboarding
- Profile screen showing green/red dot counts

**Listings**
- Post an item: category, title, description, price, single photo
- Browse feed filtered by category
- "Near me" filter (same/nearby block) vs. all-campus view

**Offers**
- Submit a cash offer with an optional short note
- Seller's offer review screen, sorted by price, with buyer's reputation visible per offer

**Deals**
- Accept an offer → locks the deal, auto-rejects other offers
- WhatsApp deep link for post-lock contact
- "Mark as completed" action, both sides, within a 3-day window
- Auto re-list + red dot on missed completion
- Green dot on mutual completion

---

## 8. Reputation System

- Every user has a visible green-dot and red-dot count.
- Green dot: +1 to both buyer and seller when a deal is mutually marked complete.
- Red dot: +1 to the buyer only, triggered automatically when the seller re-lists after the buyer fails to confirm within the completion window.
- No manual reporting — the red dot is a side effect of the re-list action, which keeps it hard to abuse.
- Dot counts show on the profile and next to each offer on the seller's review screen, so sellers can weigh a lower offer from a reliable buyer against a higher offer from a flaky one.

---

## 9. Verification Approach

- Any email allowed at signup — no campus-domain restriction.
- Hostel/block,phone numebr is self-declared, used for filtering and light proximity trust — not independently verified in v1.

---

## 10. MVP Build Plan (3–4 weeks, evenings/weekends)

- **Week 1:** Phone auth, profile setup (block field), Firestore schema, post-item screen with photo upload to Storage
- **Week 2:** Browse feed, category + "near me" filters, offer submission
- **Week 3:** Seller offer-review screen, accept flow (batch write), WhatsApp deep link, mark-as-completed + dot logic
- **Week 4 (buffer):** Polish, seed real listings, test with 8–10 actual students, fix what breaks

---

## 11. Future Considerations (not v1)

- Barter offers (buyer offers one of their own listings instead of/alongside cash)
- In-app chat or negotiation thread
- More granular no-show handling (partial refunds of trust, appeals)
- Merge with the Skill-Barter app into a single Campus Exchange product
- More categories, sub-filters (price range, posted-this-week)
