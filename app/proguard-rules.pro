# SmartCallShield R8/ProGuard Configuration

# Preservation for debugging
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
-renamesourcefileattribute SourceFile

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler { <init>(...); }
-keep class kotlinx.coroutines.internal.DiagnosticCoroutineContextException { <init>(...); }
-dontwarn kotlinx.coroutines.**

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.UnsafeCasts { *; }

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class androidx.room.RoomMasterTable { *; }
-dontwarn androidx.room.**

# Retrofit / OkHttp
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn javax.annotation.**

# GSON
-keep class com.google.gson.** { *; }
-keep @com.google.gson.annotations.SerializedName class * { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**

# Google Play Services / AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.internal.** { *; }
-keep class com.google.android.gms.dynamite.** { *; }
-dontwarn com.google.android.gms.**

# App Specific Data Models
-keep class com.akshaglobal.smartcallshield.data.model.** { *; }

# General Android
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep Compose internal classes
-keep class androidx.compose.runtime.Recomposer { *; }
-dontwarn androidx.compose.**

# Prevent shrinking of native method classes
-keepclasseswithmembernames class * {
    native <methods>;
}

# Apache POI / Log4j (Missing classes in Android environment)
-dontwarn org.apache.poi.**
-dontwarn org.apache.logging.log4j.**
-dontwarn aQute.bnd.annotation.spi.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.framework.**
-dontwarn javax.xml.stream.**
-dontwarn com.sun.xml.internal.stream.**
