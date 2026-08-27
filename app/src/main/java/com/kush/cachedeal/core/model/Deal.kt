package com.kush.cachedeal.core.model

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
    val status: String = "LOCKED",
    val timestamp: Long = System.currentTimeMillis()
)
