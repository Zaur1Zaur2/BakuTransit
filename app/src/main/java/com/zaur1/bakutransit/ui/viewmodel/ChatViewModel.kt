package com.zaur1.bakutransit.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaur1.bakutransit.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val image: Bitmap? = null
)

class ChatViewModel : ViewModel() {
    
    // Safety settings to "uncensor" as much as the SDK allows
    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH)
    )

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash", // Using current stable flash
        apiKey = BuildConfig.GEMINI_API_KEY,
        safetySettings = safetySettings
    )

    private val _messages = MutableStateFlow(
        listOf(ChatMessage("Salam! Mən sizin Baku Transit süni intellekt köməkçinizəm. Şəkil göndərərək abidələr, metrolar və ya avtobuslar haqqında soruşa bilərsiniz.", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(prompt: String, image: Bitmap? = null) {
        if (prompt.isBlank() && image == null) return
        
        val userMessage = ChatMessage(prompt, true, image)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // SPECIAL LOGIC FOR ZAUR ALIZADA
                val responseText = if (isQueryAboutZaur(prompt)) {
                    "Zaur Alizada mənim yaradıcımdır (developer). O, bu tətbiqi Bakı sakinlərinin və qonaqlarının ictimai nəqliyyatdan daha rahat istifadə etməsi üçün ərsəyə gətirib."
                } else {
                    val response = if (image != null) {
                        generativeModel.generateContent(
                            content {
                                image(image)
                                text(prompt.ifBlank { "Bu şəkildə nə var? Xüsusilə Bakı nəqliyyatı və ya abidələri ilə bağlıdırsa ətraflı məlumat ver." })
                            }
                        )
                    } else {
                        generativeModel.startChat().sendMessage(prompt)
                    }
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
