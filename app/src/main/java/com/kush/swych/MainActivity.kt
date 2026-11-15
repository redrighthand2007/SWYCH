package com.kush.swych

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.SessionState
import com.kush.swych.core.designsystem.theme.SwychTheme
import com.kush.swych.core.notifications.DealCheckWorker
import com.kush.swych.core.notifications.DealNotifier
import com.kush.swych.core.util.SettingsManager
import com.kush.swych.ui.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Keep the logo up until the saved login is read, so launch goes straight from the
        // logo to the right screen instead of showing an empty frame in between
        splash.setKeepOnScreenCondition { authRepository.sessionState.value == SessionState.Loading }

        SettingsManager.init(applicationContext)
        DealNotifier.createChannel(applicationContext)
        DealCheckWorker.schedule(applicationContext)

        enableEdgeToEdge()
        setContent {
            val themeMode by SettingsManager.themeMode.collectAsState()
            val useDarkTheme = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            SwychTheme(darkTheme = useDarkTheme) {
                AppNavHost()
            }
        }
    }
}
