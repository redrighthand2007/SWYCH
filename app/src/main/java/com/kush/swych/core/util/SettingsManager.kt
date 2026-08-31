package com.kush.swych.core.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SettingsManager {
    private const val PREFS_NAME = "swych_settings"
    private const val KEY_THEME = "theme_mode" // 0 = System, 1 = Light, 2 = Dark
    private const val KEY_HAPTIC = "haptic_feedback"

    private lateinit var prefs: SharedPreferences

    private val _themeMode = MutableStateFlow(2) // Default to Dark
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _themeMode.value = prefs.getInt(KEY_THEME, 2)
        _hapticEnabled.value = prefs.getBoolean(KEY_HAPTIC, true)
    }

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME, mode).apply()
        _themeMode.value = mode
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _hapticEnabled.value = enabled
    }
}
