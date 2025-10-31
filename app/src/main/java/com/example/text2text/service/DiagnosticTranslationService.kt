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
 * Diagnostic translation service with extensive logging
 */
class DiagnosticTranslationService {
    
    companion object {
        private const val TAG = "DiagnosticTranslation"
    }
    
    suspend fun translateText(text: String, direction: TranslationDirection): TranslationResult {
        Log.d(TAG, "=== TRANSLATION DIAGNOSTIC START ===")
        Log.d(TAG, "Input text: '$text'")
        Log.d(TAG, "Direction: ${direction.displayName}")
        Log.d(TAG, "Source language: ${direction.sourceLanguage.mlKitCode}")
        Log.d(TAG, "Target language: ${direction.targetLanguage.mlKitCode}")
        
        if (text.isBlank()) {
            Log.d(TAG, "Text is blank, returning error")
            return TranslationResult.Error("Please enter text to translate")
        }
        
        return withContext(Dispatchers.IO) {
            var translator: com.google.mlkit.nl.translate.Translator? = null
            try {
                Log.d(TAG, "Creating translator options...")
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(direction.sourceLanguage.mlKitCode)
                    .setTargetLanguage(direction.targetLanguage.mlKitCode)
                    .build()
                
                Log.d(TAG, "Getting translation client...")
                translator = Translation.getClient(options)
                Log.d(TAG, "Translation client created successfully")
                
                Log.d(TAG, "Starting model download...")
                val downloadTask = translator.downloadModelIfNeeded()
                
                try {
                    Log.d(TAG, "Waiting for model download (30s timeout)...")
                    Tasks.await(downloadTask, 30, TimeUnit.SECONDS)
                    Log.d(TAG, "✅ Model download completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Model download failed", e)
                    return@withContext TranslationResult.Error("Model download failed: ${e.message}")
                }
                
                Log.d(TAG, "Starting translation...")
                val translationTask = translator.translate(text.trim())
                
                try {
                    Log.d(TAG, "Waiting for translation (10s timeout)...")
                    val result = Tasks.await(translationTask, 10, TimeUnit.SECONDS)
                    Log.d(TAG, "✅ Translation completed")
                    Log.d(TAG, "Original: '$text'")
                    Log.d(TAG, "Translated: '$result'")
                    
                    if (result.isNullOrBlank()) {
                        Log.e(TAG, "❌ Translation returned null or empty result")
                        TranslationResult.Error("Translation returned empty result")
                    } else {
                        Log.d(TAG, "✅ Translation successful")
                        TranslationResult.Success(result)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Translation failed", e)
                    TranslationResult.Error("Translation failed: ${e.message}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error in translation service", e)
                TranslationResult.Error("Service error: ${e.message}")
            } finally {
                try {
                    translator?.close()
                    Log.d(TAG, "Translator closed")
                } catch (e: Exception) {
                    Log.e(TAG, "Error closing translator", e)
                }
                Log.d(TAG, "=== TRANSLATION DIAGNOSTIC END ===")
            }
        }
    }
}