package com.example.text2text.data

/**
 * Represents a translation history entry for offline viewing
 */
data class TranslationHistory(
    val id: String = java.util.UUID.randomUUID().toString(),
    val originalText: String,
    val translatedText: String,
    val direction: TranslationDirection,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromSpeech: Boolean = false
) {
    fun getFormattedTime(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
        return format.format(date)
    }
}