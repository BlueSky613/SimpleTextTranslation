package com.example.text2text.data

/**
 * Represents a cached translation entry with timestamp for cache management
 */
data class CacheEntry(
    val originalText: String,
    val translatedText: String,
    val targetLanguage: Language,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000L // 24 hours
    }
    
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS
    }
}