package com.example.text2text

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.text2text.ui.TranslationScreen
import com.example.text2text.ui.theme.Text2textTheme

/**
 * Main activity hosting the enhanced translation screen with speech recognition
 */
class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result is handled in the UI components
        // The speech recognition service will check permissions when needed
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request microphone permission if not already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        
        setContent {
            Text2textTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TranslationScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}