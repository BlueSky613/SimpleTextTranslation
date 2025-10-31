package com.example.text2text.service

import com.example.text2text.data.CacheEntry
import com.example.text2text.data.Language
import com.example.text2text.data.TranslationDirection
import com.example.text2text.data.TranslationResult
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import java.util.concurrent.ConcurrentHashMap

/**
 * Service class handling bidirectional translation operations with caching and ML Kit integration
 */
class TranslationService {
    
    private val translators = mutableMapOf<String, Translator>()
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    
    /**
     * Translates text using the specified translation direction
     * Uses cache for repeated translations and downloads models as needed
     */
    suspend fun translateText(text: String, direction: TranslationDirection): TranslationResult {
        if (text.isBlank()) {
            return TranslationResult.Error("Please enter text to translate")
        }
        
        // Check cache first
        val cacheKey = "${text.trim()}_${direction.name}"
        cache[cacheKey]?.let { entry ->
            if (!entry.isExpired()) {
                return TranslationResult.Success(entry.translatedText)
            } else {
                cache.remove(cacheKey)
            }
        }
        
        return try {
            val translator = getOrCreateTranslator(direction)
            
            // Try to ensure model is downloaded, but don't block if it fails
            try {
                ensureModelDownloaded(translator)
            } catch (e: Exception) {
                // If model download fails, try translation anyway - it might work with cached models
                println("Model download failed, attempting translation anyway: ${e.message}")
            }
            
            // Perform translation with timeout
            val result = withTimeout(10000) { // 10 second timeout
                translator.translate(text.trim()).await()
            }
            
            // Cache the result
            cache[cacheKey] = CacheEntry(
                originalText = text.trim(),
                translatedText = result,
                targetLanguage = direction.targetLanguage
            )
            
            TranslationResult.Success(result)
            
        } catch (e: TimeoutCancellationException) {
            TranslationResult.Error("Translation timed out. Please check your connection and try again.")
        } catch (e: Exception) {
            println("Translation error: ${e.message}")
            when {
                e.message?.contains("network", ignoreCase = true) == true -> {
                    TranslationResult.Error("Network error. Please check your connection.")
                }
                e.message?.contains("model", ignoreCase = true) == true -> {
                    TranslationResult.Error("Translation model downloading. Please wait and try again.")
                }
                e.message?.contains("MlKitException", ignoreCase = true) == true -> {
                    TranslationResult.Error("Translation service unavailable. Please try again.")
                }
                else -> {
                    TranslationResult.Error("Translation failed: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }
    
    /**
     * Gets or creates a translator for the specified translation direction
     */
    private fun getOrCreateTranslator(direction: TranslationDirection): Translator {
        val translatorKey = "${direction.sourceLanguage.name}_${direction.targetLanguage.name}"
        return translators.getOrPut(translatorKey) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(direction.sourceLanguage.mlKitCode)
                .setTargetLanguage(direction.targetLanguage.mlKitCode)
                .build()
            Translation.getClient(options)
        }
    }
    
    /**
     * Ensures the translation model is downloaded before use with timeout
     */
    private suspend fun ensureModelDownloaded(translator: Translator) {
        withTimeout(30000) { // 30 second timeout for model download
            translator.downloadModelIfNeeded().await()
        }
    }
    
    /**
     * Extension function to convert Google Play Services Task to suspend function with timeout
     */
    private suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.resumeWithException(Exception("Task was cancelled"))
        }
    }
    
    /**
     * Preloads translation models for better performance
     */
    suspend fun preloadModels() {
        TranslationDirection.values().forEach { direction ->
            try {
                val translator = getOrCreateTranslator(direction)
                ensureModelDownloaded(translator)
            } catch (e: Exception) {
                // Silently fail for preloading - models will be downloaded on demand
            }
        }
    }
    
    /**
     * Cleans up resources and closes translators
     */
    fun cleanup() {
        translators.values.forEach { it.close() }
        translators.clear()
        cache.clear()
    }
    
    /**
     * Gets cache statistics for debugging
     */
    fun getCacheStats(): Pair<Int, Int> {
        val totalEntries = cache.size
        val expiredEntries = cache.values.count { it.isExpired() }
        return Pair(totalEntries, expiredEntries)
    }
}