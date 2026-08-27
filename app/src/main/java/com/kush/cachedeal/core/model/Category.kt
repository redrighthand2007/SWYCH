package com.kush.cachedeal.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
enum class Category(val displayName: String) {
    EATABLES("Eatables"),
    WEARABLES("Wearables"),
    CYCLES("Cycles"),
    CALCULATORS("Calculators"),
    LAB_COATS("Lab Coats"),
    SUBSCRIPTIONS("Subscription Plans"),
    STUDY_NOTES("Study Notes"),
    GAME_ACCOUNTS("Game Accounts")
}

