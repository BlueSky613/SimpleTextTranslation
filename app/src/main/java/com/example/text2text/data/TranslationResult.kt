package com.example.text2text.data

/**
 * Sealed class representing the result of a translation operation
 */
sealed class TranslationResult {
    data class Success(val translatedText: String) : TranslationResult()
    data class Error(val message: String) : TranslationResult()
    object Loading : TranslationResult()
    object Idle : TranslationResult()
}