package com.kush.swych.core.util

object Constants {

    // Firestore Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_ITEMS = "items"
    const val COLLECTION_OFFERS = "offers"
    const val COLLECTION_DEALS = "deals"

    // Item Statuses
    const val STATUS_OPEN = "open"
    const val STATUS_LOCKED = "locked"
    const val STATUS_SOLD = "sold"
    const val STATUS_EXPIRED = "expired"

    // Offer Statuses
    const val STATUS_PENDING = "pending"
    const val STATUS_ACCEPTED = "accepted"
    const val STATUS_REJECTED = "rejected"

    // Deal Window (in days)
    const val DEAL_COMPLETION_DAYS = 3L

    // VIT Vellore Men's Hostel Blocks
    val HOSTEL_BLOCKS = listOf(
        "MH-A", "MH-B", "MH-C", "MH-D", "MH-E", "MH-F",
        "MH-G", "MH-H", "MH-J", "MH-K", "MH-L", "MH-M",
        "MH-N", "MH-P", "MH-Q", "MH-R", "MH-S", "MH-T"
    )
}
