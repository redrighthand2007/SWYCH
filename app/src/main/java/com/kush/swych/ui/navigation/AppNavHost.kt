package com.kush.swych.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kush.swych.core.network.SupabaseManager
import com.kush.swych.ui.auth.AuthScreen
import com.kush.swych.ui.auth.LoginScreen
import com.kush.swych.ui.auth.SignUpScreen
import com.kush.swych.ui.itemdetail.ItemDetailScreen
import com.kush.swych.ui.main.MainScreen
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus

@Composable
fun AppNavHost() {
    // Logged-in state comes from the Supabase session, so the app never thinks it is
    // signed in while every request is being rejected for a missing session.
    val sessionStatus by SupabaseManager.client.auth.sessionStatus.collectAsState()

    if (sessionStatus is SessionStatus.Initializing) {
        // Saved session is still loading from storage
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    val navController = rememberNavController()
    val startDest: Any = remember {
        if (sessionStatus is SessionStatus.Authenticated) MainRoute else AuthRoute
    }

    LaunchedEffect(sessionStatus) {
        if (sessionStatus is SessionStatus.NotAuthenticated) {
            val destination = navController.currentDestination
            val inApp = destination?.hasRoute<MainRoute>() == true || destination?.hasRoute<ItemDetailRoute>() == true
            if (inApp) {
                navController.navigate(AuthRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable<AuthRoute> {
            AuthScreen(navController = navController)
        }
        composable<SignUpRoute> {
            SignUpScreen(navController = navController)
        }
        composable<LoginRoute> {
            LoginScreen(navController = navController)
        }
        composable<MainRoute> {
            MainScreen(navController = navController)
        }
        composable<ItemDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ItemDetailRoute>()
            ItemDetailScreen(
                itemId = route.itemId,
                navController = navController
            )
        }
    }
}
