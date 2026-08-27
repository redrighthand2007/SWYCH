package com.kush.cachedeal.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "users")
@Serializable
data class User(
    @PrimaryKey
    val uid: String = "",
    val phone: String = "",
    val email: String = "",
    val name: String = "",
    val block: String = "",
    val greenDots: Int = 0,
    val redDots: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
