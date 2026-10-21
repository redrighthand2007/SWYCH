package com.kush.swych.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Deal(
    val id: String = "",
    @SerialName("item_id")
    val itemId: String = "",
    @SerialName("buyer_id")
    val buyerId: String = "",
    @SerialName("seller_id")
    val sellerId: String = "",
    @SerialName("item_title")
    val itemTitle: String = "",
    @SerialName("item_photo_url")
    val itemPhotoUrl: String = "",
    @SerialName("final_price")
    val finalPrice: Double = 0.0,
    val status: String = DealStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    @SerialName("accepted_at")
    val acceptedAt: String? = null,
    @SerialName("buyer_confirmed")
    val buyerConfirmed: Boolean = false,
    @SerialName("seller_confirmed")
    val sellerConfirmed: Boolean = false,
    @SerialName("cancelled_by")
    val cancelledBy: String? = null
) {
    /** Offer or accepted deal that still blocks the buyer from offering again. */
    val isLive: Boolean get() = status == DealStatus.PENDING || status == DealStatus.ACCEPTED

    /** Rejected, cancelled or expired: can be dismissed from the list. */
    val isClosed: Boolean
        get() = status == DealStatus.REJECTED || status == DealStatus.CANCELLED || status == DealStatus.EXPIRED
}

/** Deal lifecycle. Status changes happen in Supabase functions, never directly from the app. */
object DealStatus {
    const val PENDING = "PENDING"
    const val ACCEPTED = "ACCEPTED"
    const val COMPLETED = "COMPLETED"
    const val REJECTED = "REJECTED"
    const val CANCELLED = "CANCELLED"
    const val EXPIRED = "EXPIRED"
}
