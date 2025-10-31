package com.example.text2text.data

/**
 * Sealed class representing the result of speech recognition
 */
sealed class SpeechRecognitionResult {
    data class Success(val recognizedText: String) : SpeechRecognitionResult()
    data class Error(val message: String) : SpeechRecognitionResult()
    object Listening : SpeechRecognitionResult()
    object Idle : SpeechRecognitionResult()
}