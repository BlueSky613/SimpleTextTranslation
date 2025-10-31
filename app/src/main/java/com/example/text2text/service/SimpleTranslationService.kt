package com.example.text2text.service

import android.util.Log
import com.example.text2text.data.TranslationDirection
import com.example.text2text.data.TranslationResult
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Working translation service using ML Kit with synchronous approach
 */
class SimpleTranslationService {
    
    companion object {
        private const val TAG = "SimpleTranslationService"
    }
    
    suspend fun translateText(text: String, direction: TranslationDirection): TranslationResult {
        if (text.isBlank()) {
            return TranslationResult.Error("Please enter text to translate")
        }
        
        Log.d(TAG, "Starting translation: '$text' (${direction.displayName})")
        
        return withContext(Dispatchers.IO) {
            var translator: com.google.mlkit.nl.translate.Translator? = null
            try {
                // Create translator options
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(direction.sourceLanguage.mlKitCode)
                    .setTargetLanguage(direction.targetLanguage.mlKitCode)
                    .build()
                
                translator = Translation.getClient(options)
                Log.d(TAG, "Created translator for ${direction.sourceLanguage.mlKitCode} -> ${direction.targetLanguage.mlKitCode}")
                
                // Download model if needed (synchronous with timeout)
                val downloadTask = translator.downloadModelIfNeeded()
                try {
                    Tasks.await(downloadTask, 30, TimeUnit.SECONDS)
                    Log.d(TAG, "Model download completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Model download failed", e)
                    return@withContext TranslationResult.Error("Failed to download translation model. Please check your internet connection.")
                }
                
                // Perform translation (synchronous with timeout)
                val translationTask = translator.translate(text.trim())
                try {
                    val result = Tasks.await(translationTask, 10, TimeUnit.SECONDS)
                    Log.d(TAG, "Translation successful: '$result'")
                    
                    if (result.isNullOrBlank()) {
                        TranslationResult.Error("Translation returned empty result")
                    } else {
                        TranslationResult.Success(result)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Translation failed", e)
                    TranslationResult.Error("Translation failed: ${e.message}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error", e)
                TranslationResult.Error("Translation service error: ${e.message}")
            } finally {
                translator?.close()
                Log.d(TAG, "Translator closed")
            }
        }
    }
}