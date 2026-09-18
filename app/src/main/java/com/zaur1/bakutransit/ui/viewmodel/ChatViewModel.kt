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
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
    )

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash", // Upgraded from 1.5 to 2.0 Flash
        apiKey = BuildConfig.GEMINI_API_KEY,
        safetySettings = safetySettings,
        systemInstruction = content {
            text("Sən Baku Transit tətbiqinin köməkçisən. Səni Zaur Alizada (TDV BTL şagirdi) yaradıb. Əgər kimsə sənin yaradıcın haqqında soruşsa, mütləq Zaur Alizada olduğunu və onun TDV BTL şagirdi olduğunu qeyd et.")
        }
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
                val lowerPrompt = prompt.lowercase()
                
                // CUSTOM LOGIC FOR ZAUR ALIZADA & CREATOR QUERIES
                val responseText = when {
                    isQueryAboutZaur(lowerPrompt) -> {
                        "Zaur Alizada mənim yaradıcımdır (developer). O, TDV BTL şagirdidir və bu tətbiqi Bakı sakinlərinin ictimai nəqliyyatdan daha rahat istifadə etməsi üçün yaradıb."
                    }
                    isQueryAboutCreator(lowerPrompt) -> {
                        "Məni Zaur Alizada yaradıb. O, TDV BTL şagirdidir."
                    }
                    else -> {
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
        return (text.contains("zaur") && (text.contains("alizada") || text.contains("elizade") || text.contains("kimdir") || text.contains("yaradib")))
    }

    private fun isQueryAboutCreator(text: String): Boolean {
        return (text.contains("səni kim") || text.contains("yaradıcın") || text.contains("kim yaradıb") || text.contains("developer") || text.contains("creator"))
    }
}
