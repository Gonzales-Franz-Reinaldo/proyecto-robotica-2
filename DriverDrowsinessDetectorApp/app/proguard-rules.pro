# Add project specific ProGuard rules here.

# ========================================
# MediaPipe - Evitar ofuscación de clases ML
# ========================================
-keep class com.google.mediapipe.** { *; }
-keepclassmembers class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# ========================================
# Protobuf (usado por MediaPipe)
# ========================================
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# ========================================
# TensorFlow Lite (backend de MediaPipe)
# ========================================
-keep class org.tensorflow.** { *; }
-keepclassmembers class org.tensorflow.** { *; }
-dontwarn org.tensorflow.**

# ========================================
# Moshi - JSON Serialization
# ========================================
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}

# ========================================
# Retrofit - Networking
# ========================================
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ========================================
# OkHttp - HTTP Client
# ========================================
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ========================================
# Room Database
# ========================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract <methods>;
}

# ========================================
# Hilt - Dependency Injection
# ========================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ========================================
# Kotlin Coroutines
# ========================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ========================================
# Data Classes (para serialización JSON)
# ========================================
-keepclassmembers class com.example.driverdrowsinessdetectorapp.data.remote.dto.** { *; }
-keepclassmembers class com.example.driverdrowsinessdetectorapp.domain.model.** { *; }
-keepclassmembers class com.example.driverdrowsinessdetectorapp.data.local.entity.** { *; }

# ========================================
# Keep line numbers for debugging
# ========================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile