package com.kush.cachedeal.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "deals")
@Serializable
data class Deal(
    @PrimaryKey
    val id: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val itemPhotoUrl: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerPhone: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val buyerPhone: String = "",
    val finalPrice: Double = 0.0,
    val lockedAt: Long = System.currentTimeMillis(),
    val completionDeadline: Long = System.currentTimeMillis() + 86400000,
    val status: String = "locked", // locked / completed / expired
    val completedAt: Long? = null
)
