package com.example.text2text.data

import com.google.mlkit.nl.translate.TranslateLanguage

/**
 * Represents supported translation languages with their display names and ML Kit language codes
 */
enum class Language(val displayName: String, val mlKitCode: String) {
    ENGLISH("English", TranslateLanguage.ENGLISH),
    FRENCH("French", TranslateLanguage.FRENCH)
}