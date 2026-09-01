package com.kush.swych.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: String = "",
    @SerialName("seller_id")
    val sellerId: String = "",
    val category: String = "OTHER",
    val title: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    @SerialName("photo_url")
    val photoUrl: String? = null,
    val status: String = "OPEN"
)

