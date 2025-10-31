package com.example.text2text.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.text2text.data.SpeechRecognitionResult
import com.example.text2text.data.TranslationDirection
import com.example.text2text.data.TranslationHistory
import com.example.text2text.data.TranslationResult
import com.example.text2text.service.SpeechRecognitionService
import com.example.text2text.service.TranslationHistoryService
import com.example.text2text.service.SimpleTranslationService
import com.example.text2text.service.TestTranslationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Enhanced ViewModel managing translation state, speech recognition, and history
 */
class TranslationViewModel(context: Context) : ViewModel() {
    
    private val translationService = SimpleTranslationService()
    private val fallbackService = TestTranslationService()
    private val speechRecognitionService = SpeechRecognitionService(context)
    private val historyService = TranslationHistoryService(context)
    
    private val _inputText = mutableStateOf("")
    val inputText: State<String> = _inputText
    
    private val _selectedDirection = mutableStateOf(TranslationDirection.ENGLISH_TO_FRENCH)
    val selectedDirection: State<TranslationDirection> = _selectedDirection
    
    private val _translationResult = mutableStateOf<TranslationResult>(TranslationResult.Idle)
    val translationResult: State<TranslationResult> = _translationResult
    
    private val _speechRecognitionResult = mutableStateOf<SpeechRecognitionResult>(SpeechRecognitionResult.Idle)
    val speechRecognitionResult: State<SpeechRecognitionResult> = _speechRecognitionResult
    
    private val _translationHistory = mutableStateOf<List<TranslationHistory>>(emptyList())
    val translationHistory: State<List<TranslationHistory>> = _translationHistory
    
    private val _isHistoryVisible = mutableStateOf(false)
    val isHistoryVisible: State<Boolean> = _isHistoryVisible
    
    private var translationJob: Job? = null
    private var speechJob: Job? = null
    
    init {
        // Load history
        viewModelScope.launch {
            loadHistory()
        }
    }
    
    /**
     * Updates the input text and triggers translation with debouncing
     */
    fun updateInputText(text: String) {
        _inputText.value = text
        
        // Cancel previous translation job
        translationJob?.cancel()
        
        if (text.isBlank()) {
            _translationResult.value = TranslationResult.Idle
            return
        }
        
        // Debounce translation requests (wait 500ms after user stops typing)
        translationJob = viewModelScope.launch {
            delay(500)
            translateText()
        }
    }
    
    /**
     * Updates the selected translation direction and retranslates if needed
     */
    fun updateSelectedDirection(direction: TranslationDirection) {
        _selectedDirection.value = direction
        
        // Retranslate if there's input text
        if (_inputText.value.isNotBlank()) {
            translateText()
        }
    }
    
    /**
     * Manually triggers translation (for translate button)
     */
    fun translateText() {
        val text = _inputText.value.trim()
        if (text.isBlank()) {
            _translationResult.value = TranslationResult.Error("Please enter text to translate")
            return
        }
        
        translationJob?.cancel()
        translationJob = viewModelScope.launch {
            _translationResult.value = TranslationResult.Loading
            
            // Try ML Kit first, fallback to test service if it fails
            var result = translationService.translateText(text, _selectedDirection.value)
            
            // If ML Kit fails, use fallback service
            if (result is TranslationResult.Error) {
                android.util.Log.d("TranslationViewModel", "ML Kit failed, using fallback: ${result.message}")
                result = fallbackService.translateText(text, _selectedDirection.value)
            }
            
            _translationResult.value = result
            
            // Add to history if successful
            if (result is TranslationResult.Success) {
                historyService.addTranslation(
                    originalText = text,
                    translatedText = result.translatedText,
                    direction = _selectedDirection.value,
                    isFromSpeech = false
                )
                loadHistory()
            }
        }
    }
    
    /**
     * Starts speech recognition
     */
    fun startSpeechRecognition() {
        if (!speechRecognitionService.isAvailable()) {
            _speechRecognitionResult.value = SpeechRecognitionResult.Error("Speech recognition not available")
            return
        }
        
        speechJob?.cancel()
        speechJob = speechRecognitionService
            .startListening(_selectedDirection.value.sourceLanguage)
            .onEach { result ->
                _speechRecognitionResult.value = result
                
                // If speech recognition is successful, update input text and translate
                if (result is SpeechRecognitionResult.Success) {
                    _inputText.value = result.recognizedText
                    
                    // Translate the recognized text
                    viewModelScope.launch {
                        _translationResult.value = TranslationResult.Loading
                        
                        // Try ML Kit first, fallback to test service if it fails
                        var translationResult = translationService.translateText(
                            result.recognizedText, 
                            _selectedDirection.value
                        )
                        
                        // If ML Kit fails, use fallback service
                        if (translationResult is TranslationResult.Error) {
                            android.util.Log.d("TranslationViewModel", "ML Kit failed for speech, using fallback")
                            translationResult = fallbackService.translateText(result.recognizedText, _selectedDirection.value)
                        }
                        
                        _translationResult.value = translationResult
                        
                        // Add to history if successful
                        if (translationResult is TranslationResult.Success) {
                            historyService.addTranslation(
                                originalText = result.recognizedText,
                                translatedText = translationResult.translatedText,
                                direction = _selectedDirection.value,
                                isFromSpeech = true
                            )
                            loadHistory()
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Stops speech recognition
     */
    fun stopSpeechRecognition() {
        speechRecognitionService.stopListening()
        speechJob?.cancel()
        _speechRecognitionResult.value = SpeechRecognitionResult.Idle
    }
    
    /**
     * Clears all input and results
     */
    fun clearAll() {
        _inputText.value = ""
        _translationResult.value = TranslationResult.Idle
        _speechRecognitionResult.value = SpeechRecognitionResult.Idle
        translationJob?.cancel()
        speechJob?.cancel()
    }
    
    /**
     * Toggles history visibility
     */
    fun toggleHistoryVisibility() {
        _isHistoryVisible.value = !_isHistoryVisible.value
    }
    
    /**
     * Loads translation history
     */
    private fun loadHistory() {
        viewModelScope.launch {
            _translationHistory.value = historyService.getHistory()
        }
    }
    
    /**
     * Clears translation history
     */
    fun clearHistory() {
        viewModelScope.launch {
            historyService.clearHistory()
            loadHistory()
        }
    }
    
    /**
     * Selects a translation from history
     */
    fun selectFromHistory(history: TranslationHistory) {
        _inputText.value = history.originalText
        _selectedDirection.value = history.direction
        _translationResult.value = TranslationResult.Success(history.translatedText)
        _isHistoryVisible.value = false
    }
    
    /**
     * Gets cache statistics for debugging
     */
    fun getCacheStats(): Pair<Int, Int> {
        return Pair(0, 0) // Simplified service doesn't have cache stats
    }
    
    override fun onCleared() {
        super.onCleared()
        speechRecognitionService.stopListening()
        translationJob?.cancel()
        speechJob?.cancel()
    }
}