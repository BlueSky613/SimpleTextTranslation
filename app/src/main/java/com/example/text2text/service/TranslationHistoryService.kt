package com.example.text2text.service

import android.content.Context
import android.content.SharedPreferences
import com.example.text2text.data.TranslationDirection
import com.example.text2text.data.TranslationHistory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for managing translation history with local storage
 */
class TranslationHistoryService(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("translation_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val HISTORY_KEY = "translation_history_list"
        private const val MAX_HISTORY_SIZE = 100
    }
    
    /**
     * Adds a new translation to history
     */
    suspend fun addTranslation(
        originalText: String,
        translatedText: String,
        direction: TranslationDirection,
        isFromSpeech: Boolean = false
    ) = withContext(Dispatchers.IO) {
        val currentHistory = getHistory().toMutableList()
        
        // Remove duplicate if exists (same original text and direction)
        currentHistory.removeAll { 
            it.originalText.equals(originalText, ignoreCase = true) && 
            it.direction == direction 
        }
        
        // Add new entry at the beginning
        val newEntry = TranslationHistory(
            originalText = originalText,
            translatedText = translatedText,
            direction = direction,
            isFromSpeech = isFromSpeech
        )
        currentHistory.add(0, newEntry)
        
        // Keep only the most recent entries
        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.subList(MAX_HISTORY_SIZE, currentHistory.size).clear()
        }
        
        // Save to SharedPreferences
        val json = gson.toJson(currentHistory)
        sharedPreferences.edit().putString(HISTORY_KEY, json).apply()
    }
    
    /**
     * Gets the translation history
     */
    suspend fun getHistory(): List<TranslationHistory> = withContext(Dispatchers.IO) {
        val json = sharedPreferences.getString(HISTORY_KEY, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<TranslationHistory>>() {}.type
                gson.fromJson<List<TranslationHistory>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Clears all translation history
     */
    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        sharedPreferences.edit().remove(HISTORY_KEY).apply()
    }
    
    /**
     * Removes a specific translation from history
     */
    suspend fun removeTranslation(translationId: String) = withContext(Dispatchers.IO) {
        val currentHistory = getHistory().toMutableList()
        currentHistory.removeAll { it.id == translationId }
        
        val json = gson.toJson(currentHistory)
        sharedPreferences.edit().putString(HISTORY_KEY, json).apply()
    }
    
    /**
     * Searches history by text content
     */
    suspend fun searchHistory(query: String): List<TranslationHistory> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext getHistory()
        
        getHistory().filter { 
            it.originalText.contains(query, ignoreCase = true) ||
            it.translatedText.contains(query, ignoreCase = true)
        }
    }
}