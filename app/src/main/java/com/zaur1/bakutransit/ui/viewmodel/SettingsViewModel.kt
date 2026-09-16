package com.zaur1.bakutransit.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString("language", "AZE") ?: "AZE")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        // Apply persisted language on init
        setLanguage(_language.value)
    }

    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        prefs.edit().putBoolean("dark_mode", newValue).apply()
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        prefs.edit().putString("language", lang).apply()
        
        val appLocale: LocaleListCompat = when (lang) {
            "ENG" -> LocaleListCompat.forLanguageTags("en")
            "RUS" -> LocaleListCompat.forLanguageTags("ru")
            else -> LocaleListCompat.forLanguageTags("az")
        }
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
