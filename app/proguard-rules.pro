# SmartCallShield ProGuard Configuration
# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-dontpreverify
-repackageclasses

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# AndroidX & Jetpack
-keep class androidx.** { *; }
-dontwarn androidx.**

# Room Database
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** getDatabase(...);
}

# DataStore
-keep class androidx.datastore.** { *; }

# Hilt Dependency Injection
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep class com.akshaglobal.smartcallshield.di.** { *; }

# Domain Models and DTOs
-keep class com.akshaglobal.smartcallshield.data.model.** { *; }
-keep class com.akshaglobal.smartcallshield.domain.model.** { *; }

# Serializable classes
-keep class * implements java.io.Serializable { *; }

# TensorFlow Lite
-keep class org.tensorflow.** { *; }
-dontwarn org.tensorflow.**

# Retrofit & GSON
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn retrofit2.**
-dontwarn com.google.gson.**

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# WorkManager
-keep class androidx.work.** { *; }
-keep @androidx.work.WorkerInject class * { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Application classes
-keep class com.akshaglobal.smartcallshield.** { *; }
-keepclassmembers class com.akshaglobal.smartcallshield.** { *; }

# Preserve R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
