package com.kush.cachedeal.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object AuthRoute

@Serializable
data object SignUpRoute

@Serializable
data object LoginRoute

@Serializable
data object MainRoute

@Serializable
data object HomeRoute

@Serializable
data class BrowseRoute(val category: String = "")

@Serializable
data object PostItemRoute

@Serializable
data class ItemDetailRoute(val itemId: String)

@Serializable
data class OffersRoute(val itemId: String)

@Serializable
data object DealsRoute

@Serializable
data object ProfileRoute

@Serializable
data object GlobalOffersRoute



