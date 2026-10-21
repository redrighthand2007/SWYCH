package com.kush.swych.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object AuthRoute

@Serializable
data object SignUpRoute

@Serializable
data object LoginRoute

/** Home, Browse, Post, Deals and Profile are tabs inside MainScreen. */
@Serializable
data object MainRoute

@Serializable
data class ItemDetailRoute(val itemId: String)
