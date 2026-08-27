package com.kush.cachedeal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kush.cachedeal.ui.auth.AuthScreen
import com.kush.cachedeal.ui.itemdetail.ItemDetailScreen
import com.kush.cachedeal.ui.main.MainScreen
import com.kush.cachedeal.ui.offers.OffersScreen
import com.kush.cachedeal.ui.navigation.OffersRoute
import com.kush.cachedeal.ui.navigation.GlobalOffersRoute

import com.kush.cachedeal.core.data.AuthRepository

import android.content.Context
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppNavHost() {
    val context = LocalContext.current
    val navController = rememberNavController()
    
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val uid = prefs.getString("current_uid", null)
    val startDest = if (uid != null) MainRoute else AuthRoute
    
    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable<AuthRoute> {
            AuthScreen(navController = navController)
        }
        composable<SignUpRoute> {
            com.kush.cachedeal.ui.auth.SignUpScreen(navController = navController)
        }
        composable<LoginRoute> {
            com.kush.cachedeal.ui.auth.LoginScreen(navController = navController)
        }
        composable<MainRoute> {
            MainScreen(navController = navController)
        }
        composable<BrowseRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<BrowseRoute>()
            MainScreen(
                navController = navController,
                initialTab = 1,
                initialBrowseCategory = route.category
            )
        }
        composable<PostItemRoute> {
            MainScreen(
                navController = navController,
                initialTab = 2
            )
        }
        composable<ItemDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ItemDetailRoute>()
            ItemDetailScreen(
                itemId = route.itemId,
                navController = navController
            )
        }
        composable<OffersRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<OffersRoute>()
            OffersScreen(
                itemId = route.itemId,
                navController = navController
            )
        }
        composable<GlobalOffersRoute> {
            com.kush.cachedeal.ui.offers.GlobalOffersScreen(navController = navController, onNavigateToMainTab = {})
        }
        composable<DealsRoute> {
            MainScreen(
                navController = navController,
                initialTab = 3
            )
        }
        composable<ProfileRoute> {
            MainScreen(
                navController = navController,
                initialTab = 4
            )
        }
    }
}







