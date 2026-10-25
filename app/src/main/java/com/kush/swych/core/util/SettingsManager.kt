package com.kush.swych.core.util

import android.content.Context
import androidx.core.content.edit
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SettingsManager {
    private const val PREFS_NAME = "swych_settings"
    private const val KEY_THEME = "theme_mode" // 0 = System, 1 = Light, 2 = Dark
    private const val KEY_HAPTIC = "haptic_feedback"
    private const val KEY_NOTIFICATIONS = "deal_notifications"

    private lateinit var prefs: SharedPreferences

    private val _themeMode = MutableStateFlow(0) // Default to System
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _themeMode.value = prefs.getInt(KEY_THEME, 0)
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
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_HAPTIC, enabled) }
        _hapticEnabled.value = enabled
    }
}
