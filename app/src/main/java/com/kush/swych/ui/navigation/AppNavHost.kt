package com.kush.swych.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.SessionState
import com.kush.swych.ui.auth.AuthScreen
import com.kush.swych.ui.auth.LoginScreen
import com.kush.swych.ui.auth.SignUpScreen
import com.kush.swych.ui.itemdetail.ItemDetailScreen
import com.kush.swych.ui.main.MainScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(authRepository: AuthRepository) : ViewModel() {
    val sessionState = authRepository.sessionState
}

@Composable
fun AppNavHost(sessionViewModel: SessionViewModel = hiltViewModel()) {
    // Logged-in state comes from the Supabase session, so the app never thinks it is
    // signed in while every request is being rejected for a missing session.
    val sessionState by sessionViewModel.sessionState.collectAsStateWithLifecycle()

    if (sessionState == SessionState.Loading) {
        // Saved session is still loading from storage
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    val navController = rememberNavController()
    val startDest: Any = remember {
        if (sessionState == SessionState.SignedIn) MainRoute else AuthRoute
    }

    // Login, sign-up, logout, account deletion and expired sessions all just change the
    // session; this is the one place that decides which part of the app to show.
    LaunchedEffect(sessionState) {
        val destination = navController.currentDestination ?: return@LaunchedEffect
        val inApp = destination.hasRoute<MainRoute>() || destination.hasRoute<ItemDetailRoute>()
        when {
            sessionState == SessionState.SignedOut && inApp ->
                navController.navigate(AuthRoute) { popUpTo(0) { inclusive = true } }
            sessionState == SessionState.SignedIn && !inApp ->
                navController.navigate(MainRoute) { popUpTo(0) { inclusive = true } }
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
        composable<ItemDetailRoute> {
            ItemDetailScreen(navController = navController)
        }
    }
}
