package com.zaur1.bakutransit.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _language = MutableStateFlow("AZE")
    val language: StateFlow<String> = _language.asStateFlow()
    
    // Trigger to reload activity
    private val _recreateTrigger = MutableStateFlow(0)
    val recreateTrigger: StateFlow<Int> = _recreateTrigger.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setLanguage(lang: String) {
        if (_language.value != lang) {
            _language.value = lang
            _recreateTrigger.value += 1
        }
    }
}
