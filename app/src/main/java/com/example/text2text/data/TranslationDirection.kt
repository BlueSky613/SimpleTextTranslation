package com.example.text2text.data

/**
 * Represents the direction of translation between English and French
 */
enum class TranslationDirection(
    val displayName: String,
    val sourceLanguage: Language,
    val targetLanguage: Language
) {
    ENGLISH_TO_FRENCH("English → French", Language.ENGLISH, Language.FRENCH),
    FRENCH_TO_ENGLISH("French → English", Language.FRENCH, Language.ENGLISH)
}