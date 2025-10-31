# Voice & Text Translator - ProGuard Rules

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep ML Kit Translation classes
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# Keep Gson classes for translation history
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep data classes for JSON serialization
-keep class com.example.text2text.data.** { *; }

# Keep speech recognition classes
-keep class android.speech.** { *; }

# Jetpack Compose rules
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep ViewModel classes
-keep class androidx.lifecycle.** { *; }

# Keep coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# General Android rules
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider