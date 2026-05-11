# eShort ProGuard Rules

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.eshort.app.data.model.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }

# Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }

# Compose
-keep class androidx.compose.** { *; }
