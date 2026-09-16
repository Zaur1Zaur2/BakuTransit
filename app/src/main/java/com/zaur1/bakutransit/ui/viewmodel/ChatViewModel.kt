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
        modelName = "gemini-3.6-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Salam! Mən sizin Baku Transit süni intellekt köməkçinizəm. Necə kömək edə bilərəm?", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("You are a specialized AI assistant for Baku Public Transit. You know about Metro (Red, Green, Purple lines) and BakuBus routes. Always be helpful and prioritize Azerbaijani language unless asked otherwise.") },
            content(role = "model") { text("Anlaşıldı. Bakı ictimai nəqliyyatı ilə bağlı bütün suallarınızı cavablandırmağa hazıram.") }
        )
    )

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val userMessage = ChatMessage(userText, true)
        _messages.value = _messages.value + userMessage

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = chat.sendMessage(userText)
                val modelText = response.text ?: "Üzr istəyirəm, cavab ala bilmədim."
                _messages.value = _messages.value + ChatMessage(modelText, false)
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("Xəta baş verdi: ${e.localizedMessage}", false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
