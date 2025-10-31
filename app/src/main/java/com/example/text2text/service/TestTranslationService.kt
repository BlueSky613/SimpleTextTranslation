package com.example.text2text.service

import com.example.text2text.data.TranslationDirection
import com.example.text2text.data.TranslationResult
import kotlinx.coroutines.delay

/**
 * Test translation service that simulates translation for debugging
 */
class TestTranslationService {
    
    suspend fun translateText(text: String, direction: TranslationDirection): TranslationResult {
        if (text.isBlank()) {
            return TranslationResult.Error("Please enter text to translate")
        }
        
        // Simulate network delay
        delay(1000)
        
        // Simple mock translations for testing
        val mockTranslations = mapOf(
            "hello" to "bonjour",
            "goodbye" to "au revoir",
            "thank you" to "merci",
            "please" to "s'il vous plaît",
            "yes" to "oui",
            "no" to "non",
            "how are you" to "comment allez-vous",
            "good morning" to "bonjour",
            "good evening" to "bonsoir",
            "excuse me" to "excusez-moi",
            
            // French to English
            "bonjour" to "hello",
            "au revoir" to "goodbye",
            "merci" to "thank you",
            "s'il vous plaît" to "please",
            "oui" to "yes",
            "non" to "no",
            "comment allez-vous" to "how are you",
            "bonsoir" to "good evening",
            "excusez-moi" to "excuse me"
        )
        
        val lowerText = text.trim().lowercase()
        val translation = mockTranslations[lowerText] ?: "Translation for '$text'"
        
        return TranslationResult.Success(translation)
    }
}