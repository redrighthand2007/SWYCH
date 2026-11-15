package com.kush.swych.core.util

import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SettingsManager {
    private const val PREFS_NAME = "swych_settings"
    private const val KEY_THEME = "theme_mode" // 0 = System, 1 = Light, 2 = Dark
    private const val KEY_THEME_APPLIED = "theme_mode_applied"
    private const val KEY_HAPTIC = "haptic_feedback"
    private const val KEY_NOTIFICATIONS = "deal_notifications"

    private lateinit var prefs: SharedPreferences
    private lateinit var appContext: Context

    private val _themeMode = MutableStateFlow(0) // Default to System
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _themeMode.value = prefs.getInt(KEY_THEME, 0)
        applyToSystem(_themeMode.value)
        _hapticEnabled.value = prefs.getBoolean(KEY_HAPTIC, true)
        _notificationsEnabled.value = prefs.getBoolean(KEY_NOTIFICATIONS, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIFICATIONS, enabled) }
        _notificationsEnabled.value = enabled
    }

    fun setThemeMode(mode: Int) {
        prefs.edit { putInt(KEY_THEME, mode) }
        _themeMode.value = mode
        applyToSystem(mode)
    }

    /**
     * Tells Android which theme the app uses, so the launch screen shown before the app
     * draws matches it (otherwise a dark-mode app opens with a white flash on a light phone).
     */
    private fun applyToSystem(mode: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        // Telling Android restarts the screen, so only do it when the choice actually changed
        if (prefs.getInt(KEY_THEME_APPLIED, -1) == mode) return
        prefs.edit { putInt(KEY_THEME_APPLIED, mode) }
        val night = when (mode) {
            1 -> UiModeManager.MODE_NIGHT_NO
            2 -> UiModeManager.MODE_NIGHT_YES
            else -> UiModeManager.MODE_NIGHT_AUTO
        }
        appContext.getSystemService(UiModeManager::class.java)?.setApplicationNightMode(night)
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_HAPTIC, enabled) }
        _hapticEnabled.value = enabled
    }
}
