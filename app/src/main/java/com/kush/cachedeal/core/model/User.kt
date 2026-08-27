package com.kush.cachedeal.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String = "",
    val phone: String = "",
    val email: String = "",
    val name: String = "",
    val block: String = "",
    @SerialName("green_dots")
    val greenDots: Int = 0,
    @SerialName("red_dots")
    val redDots: Int = 0,
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
