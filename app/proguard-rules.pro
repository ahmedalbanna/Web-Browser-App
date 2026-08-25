# Milestone 5 v1.4 — Play Store hardening (R8)
# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable,Signature,*Annotation*
-renamesourcefileattribute SourceFile

# WebView JS interfaces
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepclassmembers class com.example.ui.BrowserWebView* { *; }

# Room — keep entities, DAOs, DB
-keep class com.example.data.** { *; }
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Moshi — keep generated adapters
-keep class com.squareup.moshi.** { *; }
-keep class **JsonAdapter { *; }
-keepclasseswithmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonClass class *
-dontwarn com.squareup.moshi.**

# Retrofit / OkHttp
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Firebase (if enabled in v1.4)
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Compose — keep Composable signatures for R8
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Secrets Gradle Plugin — keep BuildConfig
-keep class com.example.BuildConfig { *; }

# WebKit Proxy
-keep class androidx.webkit.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
