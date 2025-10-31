# Voice & Text Translator Android App

A modern Android application for bidirectional English-French translation with both text input and speech recognition capabilities using Google ML Kit's on-device translation.

## Features

- **Bidirectional Translation**: English ↔ French translation support
- **Voice Input**: Speech-to-text recognition with instant translation
- **Text Input**: Traditional typing with real-time translation
- **Translation History**: Local storage of translations for offline viewing
- **Fast Performance**: <2 second response times with smart caching
- **Offline Capable**: Works without internet after initial model downloads
- **Modern UI**: Material Design 3 interface with Jetpack Compose
- **Smart Caching**: 24-hour cache for repeated phrases
- **Copy to Clipboard**: One-tap copying of translations

## Supported Translation Directions

- English → French
- French → English

## Build Instructions

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK API 24+ (Android 7.0)
- Internet connection for initial ML Kit model downloads
- Microphone permission for speech recognition

### Setup

1. **Clone and Open Project**
   ```bash
   git clone <repository-url>
   cd text-translator-android
   ```
   Open the project in Android Studio

2. **Sync Dependencies**
   - Android Studio will automatically prompt to sync Gradle files
   - Or manually: `File → Sync Project with Gradle Files`

3. **Build the App**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use Android Studio: `Build → Make Project`

4. **Run on Device/Emulator**
   - Connect Android device with USB debugging enabled
   - Or start an Android emulator (API 24+)
   - Click `Run` in Android Studio or use: `./gradlew installDebug`

### Release Build

To create a release APK:

```bash
./gradlew assembleRelease
```

The APK will be generated at: `app/build/outputs/apk/release/app-release-unsigned.apk`

For a signed release, configure signing in `app/build.gradle.kts` and use:
```bash
./gradlew assembleRelease
```

## API Usage Notes

### Translation & Speech Recognition APIs

This app uses **Google ML Kit's Translation API** and **Android's built-in Speech Recognition**:

**Translation (ML Kit)**:
- **On-Device Processing**: No data sent to servers after initial model download
- **Model Downloads**: ~30-50MB per language pair (one-time download)
- **Performance**: Typically <500ms for short sentences, <2s for longer paragraphs
- **Accuracy**: Production-quality translations suitable for most use cases

**Speech Recognition (Android)**:
- **Built-in Service**: Uses Android's native speech recognition
- **Language Support**: English and French speech recognition
- **Real-time Processing**: Instant speech-to-text conversion
- **Offline Capability**: Works with downloaded language models

### Key Implementation Details

1. **Translation Service** (`TranslationService.kt`)
   - Manages bidirectional ML Kit translator instances
   - Handles model downloading and caching
   - Provides error handling with user-friendly messages

2. **Speech Recognition Service** (`SpeechRecognitionService.kt`)
   - Android speech recognition integration
   - Language-specific recognition (English/French)
   - Real-time speech-to-text conversion

3. **History Service** (`TranslationHistoryService.kt`)
   - Local storage using SharedPreferences and JSON
   - Translation history with timestamps
   - Search and management capabilities

4. **Enhanced ViewModel** (`TranslationViewModel.kt`)
   - Manages both text and speech input
   - Debounced input handling (500ms delay)
   - Lifecycle-aware resource management
   - History management integration

## Project Structure

```
app/src/main/java/com/example/text2text/
├── data/
│   ├── Language.kt                 # Supported language definitions
│   ├── TranslationDirection.kt     # Translation direction enum
│   ├── TranslationResult.kt        # Translation result states
│   ├── SpeechRecognitionResult.kt  # Speech recognition states
│   ├── TranslationHistory.kt       # History data model
│   └── CacheEntry.kt              # Cache data structure
├── service/
│   ├── TranslationService.kt      # ML Kit integration & caching
│   ├── SpeechRecognitionService.kt # Speech recognition service
│   └── TranslationHistoryService.kt # History management
├── viewmodel/
│   └── TranslationViewModel.kt    # Enhanced UI state management
├── ui/
│   ├── TranslationScreen.kt       # Main screen with voice & text
│   └── components/
│       ├── DirectionSelector.kt   # Translation direction picker
│       ├── EnhancedInputSection.kt # Text + voice input
│       ├── EnhancedOutputSection.kt # Enhanced translation display
│       └── TranslationHistoryDialog.kt # History dialog
└── MainActivity.kt                # Entry point with permissions
```

## Performance Considerations

- **Model Preloading**: Translation models downloaded in background on app start
- **Debounced Input**: Translation requests delayed 500ms after user stops typing
- **Memory Management**: Proper cleanup of translators and speech recognizers
- **Cache Efficiency**: Automatic removal of expired cache entries
- **History Storage**: Efficient JSON-based local storage with size limits

## Network & Permission Requirements

- **Initial Setup**: Internet required for downloading ML Kit models (~60MB for both directions)
- **Runtime**: Fully offline after models are downloaded
- **Microphone Permission**: Required for speech recognition functionality
- **Fallback**: Clear error messages for network/permission issues

## App Usage Guide

### First Launch
1. Grant microphone permission when prompted
2. App downloads ML Kit models for English-French translation
3. Models cached locally for offline use

### Text Translation
1. Select translation direction (English → French or French → English)
2. Type text in the input field
3. Translation appears automatically after 500ms delay
4. Tap copy button to copy translation to clipboard

### Voice Translation
1. Select appropriate translation direction
2. Tap the microphone button
3. Speak clearly when "Listening..." appears
4. Speech is converted to text and automatically translated
5. Results saved to history for offline access

### Translation History
1. Tap history icon in top-right corner
2. Browse previous translations (text and voice)
3. Tap any entry to reuse it
4. Clear history with trash icon if needed

## Testing

The app includes comprehensive error handling for:
- Network connectivity issues
- Model download failures  
- Translation service errors
- Invalid input handling

## Future Enhancements

Potential improvements for future versions:
- Additional language pairs (Spanish, German, Italian, etc.)
- Text-to-speech output for translations
- Conversation mode with alternating languages
- Photo translation using camera input
- Batch translation support
- Translation confidence scores
- Cloud sync for translation history
- Offline speech recognition models

## License

This project is provided as-is for demonstration purposes.