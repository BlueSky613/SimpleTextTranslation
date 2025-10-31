package com.example.text2text.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.text2text.data.SpeechRecognitionResult
import com.example.text2text.data.TranslationDirection
import com.example.text2text.data.TranslationResult
import com.example.text2text.ui.components.DirectionSelector
import com.example.text2text.ui.components.EnhancedInputSection
import com.example.text2text.ui.components.EnhancedOutputSection
import com.example.text2text.ui.components.TranslationHistoryDialog
import com.example.text2text.viewmodel.TranslationViewModel

/**
 * Enhanced translation screen with speech recognition and history
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: TranslationViewModel = viewModel { TranslationViewModel(context) }
    
    val inputText by viewModel.inputText
    val selectedDirection by viewModel.selectedDirection
    val translationResult by viewModel.translationResult
    val speechRecognitionResult by viewModel.speechRecognitionResult
    val translationHistory by viewModel.translationHistory
    val isHistoryVisible by viewModel.isHistoryVisible
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header with History Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Voice & Text Translator",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = viewModel::toggleHistoryVisibility
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Translation History",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Direction Selector
        DirectionSelector(
            selectedDirection = selectedDirection,
            onDirectionSelected = viewModel::updateSelectedDirection
        )
        
        // Enhanced Input Section with Speech
        EnhancedInputSection(
            inputText = inputText,
            onTextChange = viewModel::updateInputText,
            onClearClick = viewModel::clearAll,
            onSpeechStart = viewModel::startSpeechRecognition,
            onSpeechStop = viewModel::stopSpeechRecognition,
            speechRecognitionResult = speechRecognitionResult,
            sourceLanguage = selectedDirection.sourceLanguage
        )
        
        // Translate Button
        Button(
            onClick = viewModel::translateText,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = inputText.isNotBlank() && translationResult !is TranslationResult.Loading
        ) {
            if (translationResult is TranslationResult.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (translationResult is TranslationResult.Loading) "Translating..." else "Translate",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Enhanced Output Section
        EnhancedOutputSection(
            translationResult = translationResult,
            targetLanguage = selectedDirection.targetLanguage
        )
    }
    
    // Translation History Dialog
    if (isHistoryVisible) {
        TranslationHistoryDialog(
            history = translationHistory,
            onDismiss = viewModel::toggleHistoryVisibility,
            onSelectTranslation = viewModel::selectFromHistory,
            onClearHistory = viewModel::clearHistory
        )
    }
}