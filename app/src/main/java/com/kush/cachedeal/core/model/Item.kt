package com.kush.cachedeal.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "items")
@Serializable
data class Item(
    @PrimaryKey
    val id: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerBlock: String = "",
    val sellerGreenDots: Int = 0,
    val sellerRedDots: Int = 0,
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val photoUrl: String = "",
    val status: String = "open", // open / locked / sold
    val createdAt: Long = System.currentTimeMillis()
)
