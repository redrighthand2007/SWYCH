package com.kush.swych

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.kush.swych.core.designsystem.theme.SwychTheme
import com.kush.swych.ui.navigation.AppNavHost
import kotlinx.coroutines.launch

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import com.kush.swych.core.util.SettingsManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        SettingsManager.init(applicationContext)
        

        enableEdgeToEdge()
        setContent {
            val useDarkTheme = isSystemInDarkTheme()

            SwychTheme(darkTheme = useDarkTheme) {
                AppNavHost()
            }
        }
    }
}




