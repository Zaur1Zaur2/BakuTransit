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
    
    // Use a simpler boolean flag for language change to prevent "twitching"
    private val _shouldRecreate = MutableStateFlow(false)
    val shouldRecreate = _shouldRecreate.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setLanguage(lang: String) {
        if (_language.value != lang) {
            _language.value = lang
            _shouldRecreate.value = true
        }
    }

    fun onRecreated() {
        _shouldRecreate.value = false
    }
}
