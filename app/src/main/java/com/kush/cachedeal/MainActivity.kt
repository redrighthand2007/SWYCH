package com.kush.cachedeal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.kush.cachedeal.core.designsystem.theme.CacheDealTheme
import com.kush.cachedeal.ui.navigation.AppNavHost
import kotlinx.coroutines.launch

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import com.kush.cachedeal.core.util.SettingsManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        SettingsManager.init(applicationContext)
        com.kush.cachedeal.core.database.DatabaseSeeder.seedDatabase(applicationContext)

        enableEdgeToEdge()
        setContent {
            val themeMode by SettingsManager.themeMode.collectAsState()
            val useDarkTheme = when (themeMode) {
                0 -> isSystemInDarkTheme()
                1 -> false
                else -> true
            }

            CacheDealTheme(darkTheme = useDarkTheme) {
                AppNavHost()
            }
        }
    }
}
