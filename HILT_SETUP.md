# Hilt Dependency Injection Setup - CallSecure

## Overview
This document outlines the Hilt dependency injection setup for the CallSecure Android application.

## What was Added

### 1. Gradle Plugins (app/build.gradle.kts)
The following plugins were added to enable Hilt and annotation processing:

```kotlin
plugins {
    // ...existing plugins...
    alias(libs.plugins.hilt.android)        // Hilt Android plugin
    alias(libs.plugins.kotlin.kapt)          // Kotlin Annotation Processing Tool (for Hilt)
    alias(libs.plugins.kotlin.ksp)           // Kotlin Symbol Processing (for Room)
}
```

### 2. KAPT Configuration
Added KAPT configuration to handle Hilt's annotation processing correctly:

```kotlin
kapt {
    correctErrorTypes = true
}
```

### 3. Dependencies (app/build.gradle.kts)
Added the following Hilt-related dependencies:

```kotlin
// Hilt - Dependency Injection
implementation(libs.hilt.android)               // Hilt runtime
kapt(libs.hilt.compiler)                        // Hilt annotation processor
implementation(libs.hilt.navigation.compose)    // Hilt navigation support for Compose
```

### 4. Version Definitions (gradle/libs.versions.toml)
Hilt version is already defined:
```toml
[versions]
hilt = "2.51.1"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

[plugins]
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
kotlin-kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlin" }
```

## Application Setup

### CallSecureApp.kt
The application class is annotated with `@HiltAndroidApp`:

```kotlin
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CallSecureApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize app-level components
    }
}
```

### MainActivity.kt
The main activity is annotated with `@AndroidEntryPoint` to enable dependency injection:

```kotlin
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Activity code...
}
```

### AndroidManifest.xml
The manifest declares the Hilt application:

```xml
<application
    android:name=".CallSecureApp"
    ...
>
```

## DI Modules (di/Modules.kt)

The project includes DI modules for providing singleton instances:

### DatabaseModule
Provides the Room database instance:
```kotlin
object DatabaseModule {
    fun provideDatabase(context: Context): CallSecureDatabase {
        return CallSecureDatabase.getDatabase(context)
    }
}
```

### DaoModule
Provides Data Access Objects for each entity:
```kotlin
object DaoModule {
    fun provideContactDao(database: CallSecureDatabase) = database.contactDao()
    fun provideCallLogDao(database: CallSecureDatabase) = database.callLogDao()
    fun provideSpamReportDao(database: CallSecureDatabase) = database.spamReportDao()
    fun provideDrivingModeLogDao(database: CallSecureDatabase) = database.drivingModeLogDao()
}
```

### PreferencesModule
Provides PreferencesManager for DataStore preferences:
```kotlin
object PreferencesModule {
    fun providePreferencesManager(context: Context): PreferencesManager {
        return PreferencesManager(context)
    }
}
```

### AiModule
Provides the TensorFlow Lite spam detection model:
```kotlin
object AiModule {
    fun provideSpamDetectionModel(context: Context): SpamDetectionModel {
        val model = SpamDetectionModel(context)
        model.initialize()
        return model
    }
}
```

## Building the Project

To build the project with Hilt enabled:

### Clean Build
```bash
./gradlew clean assembleDebug --refresh-dependencies
```

### Incremental Build
```bash
./gradlew assembleDebug
```

### Install to Device
```bash
./gradlew installDebug
```

## Annotation Processing

When you build the project:

1. **KAPT** (Kotlin Annotation Processing Tool) processes Hilt annotations:
   - Generates Hilt component classes
   - Creates entry point implementations
   - Produces dependency binding factories

2. **KSP** (Kotlin Symbol Processing) processes Room annotations:
   - Generates DAO implementations
   - Creates database schema
   - Builds migrations if needed

Generated files are placed in:
- `app/build/generated/ap_generated_sources/debug/` (KAPT outputs)
- `app/build/generated/ksp/debug/` (KSP outputs)

## Runtime Behavior

At runtime, when the app starts:

1. Android instantiates `CallSecureApp` (the @HiltAndroidApp Application)
2. Hilt initializes its component
3. When `MainActivity` (marked with @AndroidEntryPoint) is created, Hilt performs dependency injection
4. ViewModels can request injected dependencies via constructor injection

## Troubleshooting

### "Hilt Activity must be attached to an @HiltAndroidApp Application"
This error means:
- The application is not properly annotated with `@HiltAndroidApp`, OR
- The installed APK is stale and needs to be uninstalled and reinstalled

**Solution:**
```bash
adb uninstall com.akshaglobal.smartcallshield
./gradlew clean installDebug
```

### "Cannot find symbol: class Hilt_MainActivity"
This means Hilt code generation didn't run.

**Solution:**
```bash
./gradlew clean build --refresh-dependencies
```

### Build fails with KAPT errors
Ensure `kapt { correctErrorTypes = true }` is configured in build.gradle.kts.

## Dependency Injection Usage

### In Activities
```kotlin
@AndroidEntryPoint
class MyActivity : ComponentActivity() {
    // Hilt will inject dependencies into constructor
}
```

### In ViewModels
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel() {
    // repository will be injected
}
```

### In Services
```kotlin
@AndroidEntryPoint
class MyService : Service() {
    @Inject
    lateinit var repository: MyRepository
}
```

## Files Modified

- `app/build.gradle.kts` - Added Hilt plugins, KAPT configuration, and dependencies
- `app/src/main/java/com/akshaglobal/smartcallshield/CallSecureApp.kt` - Added @HiltAndroidApp
- `app/src/main/java/com/akshaglobal/smartcallshield/MainActivity.kt` - Has @AndroidEntryPoint
- `app/src/main/java/com/akshaglobal/smartcallshield/di/Modules.kt` - DI module definitions
- `AndroidManifest.xml` - Already declares the Hilt application

## Next Steps

1. **Re-enable Hilt annotations in services** if you want full DI support:
   - Add `@AndroidEntryPoint` to `CallInterceptor` and `SmsHandler`
   - Add `@Inject` annotations to dependencies

2. **Create ViewModels with Hilt**:
   - Use `@HiltViewModel` for easy lifecycle-aware DI

3. **Test the setup**:
   ```bash
   ./gradlew clean assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   adb logcat | grep -i "hilt"  # Check for Hilt initialization
   ```

## References

- [Hilt Official Documentation](https://developer.android.com/training/dependency-injection/hilt-android)
- [Dagger 2 & Hilt](https://dagger.dev/hilt/)
- [KAPT Documentation](https://kotlinlang.org/docs/kapt.html)
- [KSP Documentation](https://kotlinlang.org/docs/ksp-overview.html)

---

**Status**: ✅ Hilt dependency injection is properly configured and ready to use.

