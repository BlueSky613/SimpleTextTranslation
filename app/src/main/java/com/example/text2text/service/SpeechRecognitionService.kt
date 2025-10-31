package com.example.text2text.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.text2text.data.Language
import com.example.text2text.data.SpeechRecognitionResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

/**
 * Service for handling speech recognition using Android's built-in speech recognizer
 */
class SpeechRecognitionService(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    
    /**
     * Starts speech recognition for the specified language
     * Returns a Flow that emits recognition results
     */
    fun startListening(language: Language): Flow<SpeechRecognitionResult> = callbackFlow {
        // Check if speech recognition is available
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            trySend(SpeechRecognitionResult.Error("Speech recognition not available on this device"))
            close()
            return@callbackFlow
        }
        
        // Create speech recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        
        // Set up recognition listener
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(SpeechRecognitionResult.Listening)
            }
            
            override fun onBeginningOfSpeech() {
                // Speech input has begun
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changed - could be used for visual feedback
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // Partial audio buffer received
            }
            
            override fun onEndOfSpeech() {
                // Speech input has ended
            }
            
            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
                    else -> "Unknown error occurred"
                }
                trySend(SpeechRecognitionResult.Error(errorMessage))
                close()
            }
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    trySend(SpeechRecognitionResult.Success(recognizedText))
                } else {
                    trySend(SpeechRecognitionResult.Error("No speech recognized"))
                }
                close()
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                // Partial results - could be used for real-time feedback
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Additional events
            }
        })
        
        // Create recognition intent
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, getLanguageCode(language))
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, getLanguageCode(language))
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        // Start listening
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            trySend(SpeechRecognitionResult.Error("Failed to start speech recognition: ${e.message}"))
            close()
        }
        
        // Cleanup when flow is cancelled
        awaitClose {
            stopListening()
        }
    }
    
    /**
     * Stops speech recognition
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
    
    /**
     * Maps our Language enum to Android speech recognition language codes
     */
    private fun getLanguageCode(language: Language): String {
        return when (language) {
            Language.ENGLISH -> Locale.ENGLISH.toString()
            Language.FRENCH -> Locale.FRENCH.toString()
        }
    }
    
    /**
     * Checks if speech recognition is available on the device
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }
}