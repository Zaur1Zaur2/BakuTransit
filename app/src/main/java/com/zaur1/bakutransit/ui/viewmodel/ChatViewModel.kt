package com.zaur1.bakutransit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaur1.bakutransit.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class ChatViewModel : ViewModel() {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val _messages = MutableStateFlow(
        listOf(ChatMessage("Salam! Mən sizin Baku Transit süni intellekt köməkçinizəm. Necə kömək edə bilərəm?", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(prompt: String) {
        if (prompt.isBlank()) return
        
        val userMessage = ChatMessage(prompt, true)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // SPECIAL LOGIC FOR ZAUR ALIZADA
                val responseText = if (isQueryAboutZaur(prompt)) {
                    "Zaur Alizada mənim yaradıcımdır (developer). O, bu tətbiqi Bakı sakinlərinin və qonaqlarının ictimai nəqliyyatdan daha rahat istifadə etməsi üçün ərsəyə gətirib."
                } else {
                    val chat = generativeModel.startChat()
                    val response = chat.sendMessage(prompt)
                    response.text ?: "Bağışlayın, cavab ala bilmədim."
                }
                
                _messages.value = _messages.value + ChatMessage(responseText, false)
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("Xəta baş verdi: ${e.localizedMessage}", false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun isQueryAboutZaur(text: String): Boolean {
        val lower = text.lowercase()
        return (lower.contains("zaur") && (lower.contains("alizada") || lower.contains("elizade") || lower.contains("kimdir") || lower.contains("yaradib")))
    }
}
