# FCFS Deal System Implementation Plan

## Goal Description
Implement a First-Come-First-Serve (FCFS) deal system where a buyer applying for a deal marks the item as `PENDING`. The seller then has the choice to Accept or Reject the deal from their Deals section. We will also add Pull-to-Refresh functionality across the app.

## Proposed Changes

### Database Layer
#### [MODIFY] app/src/main/java/com/kush/swych/core/model/Deal.kt
- Change default `status` from `"LOCKED"` to `"PENDING"`.

#### [MODIFY] app/src/main/java/com/kush/swych/core/data/DealRepository.kt
- Update `createDeal` to set the Item status to `"PENDING"` instead of `"LOCKED"`.
- Add a new method `updateDealStatus(dealId: String, itemId: String, newStatus: String)`:
  - If `newStatus == "SOLD"`: Updates deal to `"SOLD"` and item to `"SOLD"`.
  - If `newStatus == "REJECTED"`: Updates deal to `"REJECTED"` and item to `"OPEN"`.

#### [MODIFY] app/src/main/java/com/kush/swych/core/data/ItemRepository.kt
- Add `refreshItems()` method to force fetch from Supabase (bypassing the in-memory cache) to support Pull-to-Refresh.

### UI Layer
#### [MODIFY] app/src/main/java/com/kush/swych/ui/itemdetail/ItemDetailScreen.kt
- Update the UI to handle the new `"PENDING"` status:
  - If the item is `PENDING`, replace the "Submit Deal" button with a message: "This item is currently pending a deal."
- Add SwipeRefresh / Pull-to-refresh to fetch latest item details.

#### [MODIFY] app/src/main/java/com/kush/swych/ui/deals/DealsScreen.kt
- Redesign the `DealCard` and the sections:
  - **Buyer Section**: If status is `PENDING`, show "Waiting for seller approval". If `SOLD`, show "Deal Accepted!". If `REJECTED`, show "Offer Rejected".
  - **Seller Section**: If status is `PENDING`, show `[Accept]` and `[Reject]` buttons. If `SOLD` or `REJECTED`, just show the final status.
- Wrap the entire content in an `ExperimentalMaterial3Api` `pullRefresh` layout to allow the user to swipe down to refresh deals.

#### [MODIFY] app/src/main/java/com/kush/swych/ui/browse/BrowseScreen.kt
- Update the item grid filter to completely hide items that are `status == "SOLD"`.
- For items that are `status == "PENDING"`, display a visually distinct "PENDING" or "SOLD" overlay banner so users know it's taken.
- Implement Pull-to-Refresh.

#### [MODIFY] app/src/main/java/com/kush/swych/ui/home/HomeScreen.kt
- Add Pull-to-Refresh to the home feed.
- Hide `SOLD` items and mark `PENDING` items with an overlay badge just like the Browse Screen.

## Verification Plan
### Automated Tests
- App builds successfully without syntax errors.

### Manual Verification
- Ask the user to:
  1. Post a new item as Seller X.
  2. Log into a different account (Buyer Y).
  3. Go to the Browse screen, pull-to-refresh to see the new item.
  4. Tap the item, submit a deal.
  5. Go back to Browse screen, verify the item now has a "PENDING" badge.
  6. Log back into Seller X.
  7. Go to Deals screen, pull-to-refresh, see the pending request, and tap "Accept".
  8. Go back to Browse screen, verify the item is completely removed (Sold).
