# SmartCallShield - Quick Start Guide for Developers

## 🚀 Get Started in 5 Minutes

### 1. Prerequisites
```bash
# Check Java version (need 11+)
java -version

# Check Android SDK
echo $ANDROID_SDK_ROOT

# Clone and navigate
git clone https://github.com/akshaglobal/SmartCallShield.git
cd SmartCallShield
```

### 2. Build Project
```bash
# Build debug APK
./gradlew clean build

# Expected: BUILD SUCCESSFUL in ~2-3 minutes
```

### 3. Run on Device/Emulator
```bash
# Install on connected device/emulator
./gradlew installDebug

# Expected: App launches on device
```

### 4. Grant Permissions
- On first launch, app requests permissions
- Grant all permissions for full functionality
- Without permissions, only settings work

### 5. Explore App
- **Dashboard**: View statistics and select call mode
- **Contacts**: Add whitelist/blacklist contacts
- **Settings**: Configure spam detection and driving mode
- **Analytics**: View call statistics

---

## 📁 Project Structure at a Glance

```
app/src/main/java/com/akshaglobal/smartcallshield/

data/               ← Data Layer (Database, Repositories)
domain/             ← Domain Layer (Business Logic)
presentation/       ← Presentation Layer (UI, ViewModels)
service/            ← Services (CallInterceptor, AI, etc)
di/                 ← Dependency Injection (Hilt)
MainActivity.kt     ← Entry point
SmartCallShieldApp  ← Application class
```

---

## 🔧 Make Your First Change

### Example: Add New Setting Toggle

**Step 1: Add to PreferencesManager**
```kotlin
// In data/preferences/PreferencesManager.kt
val myNewSetting: Flow<Boolean> = context.dataStore.data.map { preferences ->
    preferences[booleanPreferencesKey("my_new_setting")] ?: false
}

suspend fun setMyNewSetting(value: Boolean) {
    // Implementation
}
```

**Step 2: Add to SettingsViewModel**
```kotlin
// In presentation/viewmodel/ViewModels.kt
private val _myNewSetting = MutableStateFlow(false)
val myNewSetting = _myNewSetting.asStateFlow()

// In init block:
viewModelScope.launch {
    preferencesManager.myNewSetting.collect {
        _myNewSetting.value = it
    }
}

fun setMyNewSetting(value: Boolean) {
    viewModelScope.launch {
        preferencesManager.setMyNewSetting(value)
    }
}
```

**Step 3: Add to SettingsScreen**
```kotlin
// In presentation/ui/screens/SettingsScreen.kt
val myNewSetting by viewModel.myNewSetting.collectAsState()

SettingCard(
    title = "My New Setting",
    description = "Description here",
    isEnabled = myNewSetting,
    onToggle = { viewModel.setMyNewSetting(it) }
)
```

**Done!** Your setting is now fully functional.

---

## 🧪 Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (on device)
./gradlew connectedAndroidTest

# With coverage report
./gradlew testDebugUnitTest jacocoTestReport
```

---

## 🐛 Common Issues & Solutions

### Issue: Build fails with "symbol not found"
**Solution**: Run `./gradlew clean build`

### Issue: App crashes on launch
**Solution**: Check logcat: `adb logcat | grep smartcallshield`

### Issue: Permissions not working
**Solution**: 
1. Grant all permissions manually
2. Check AndroidManifest.xml
3. Verify permission handling in MainActivity

### Issue: Database errors
**Solution**: Delete app data and reinstall: `adb shell pm clear com.akshaglobal.smartcallshield`

### Issue: Hilt injection errors
**Solution**: Run `./gradlew clean kspDebugKotlin`

---

## 📱 Testing on Different Android Versions

```bash
# API 29 (Android 10)
emulator -avd Android10

# API 31 (Android 12) 
emulator -avd Android12

# API 34 (Android 14)
emulator -avd Android14

# API 36 (Android 15)
emulator -avd Android15
```

---

## 📊 Key Files to Know

| File | Purpose | Modify When... |
|------|---------|----------------|
| `data/model/Entities.kt` | Database schema | Adding new data types |
| `data/repository/*.kt` | Data access | Changing queries |
| `domain/usecase/*.kt` | Business logic | Adding features |
| `presentation/ui/screens/*.kt` | UI | Changing UI |
| `service/CallInterceptor.kt` | Call handling | Changing call logic |
| `build.gradle.kts` | Dependencies | Adding libraries |
| `AndroidManifest.xml` | Permissions | Adding new permissions |

---

## 🎯 Useful Gradle Commands

```bash
# Build
./gradlew clean build              # Clean build
./gradlew build                    # Incremental build
./gradlew assembleDebug            # Build debug APK
./gradlew bundleRelease            # Build release AAB

# Test
./gradlew test                     # Unit tests
./gradlew connectedAndroidTest     # Instrumented tests

# Analysis
./gradlew lint                     # Code analysis
./gradlew spotlessCheck            # Format check

# Clean
./gradlew clean                    # Delete build files
./gradlew cleanBuildCache          # Clear build cache

# Info
./gradlew tasks                    # List all tasks
./gradlew dependencies             # Show dependencies
./gradlew buildEnvironment         # Show environment
```

---

## 🎨 UI Development Tips

### Theme Colors
Edit `ui/theme/Color.kt`:
```kotlin
val Primary = Color(0xFF2196F3)
val PrimaryDark = Color(0xFF1565C0)
val Accent = Color(0xFFFF5252)
```

### Add New Screen
1. Create file in `presentation/ui/screens/MyScreen.kt`
2. Create ViewModel in `presentation/viewmodel/ViewModels.kt`
3. Add to `navigation/Navigation.kt`
4. Add to bottom navigation

### Compose Tips
- Use `collectAsState()` for Flow
- Use `remember` for local state
- Use `Box`, `Column`, `Row` for layout
- Use `Card`, `Button`, `Text` for widgets

---

## 🔍 Debug Mode

### Enable Detailed Logging
```kotlin
// In SmartCallShieldApp.kt
override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
        enableDetailedLogging()
    }
}
```

### View Database
```bash
# In Android Studio: View > Tool Windows > Device File Explorer
# Navigate to: /data/data/com.akshaglobal.smartcallshield/databases/

# Or use SQLite viewer plugin
```

### Logcat Filtering
```bash
# Show only SmartCallShield logs
adb logcat | grep smartcallshield

# Show errors and above
adb logcat | grep "E.*smartcallshield"

# Save logs to file
adb logcat > logs.txt
```

---

## 📚 Learning Resources

### Key Concepts
- **MVVM**: ViewModel holds UI state
- **Clean Architecture**: Separation of data/domain/presentation
- **Coroutines**: Async operations without callbacks
- **Flow**: Reactive data streams
- **Room**: Type-safe database access
- **Hilt**: Automatic dependency injection

### Documentation Links
- Kotlin: https://kotlinlang.org/docs
- Jetpack: https://developer.android.com/jetpack
- Compose: https://developer.android.com/jetpack/compose
- Room: https://developer.android.com/training/data-storage/room
- Coroutines: https://kotlinlang.org/docs/coroutines-overview.html

---

## 🚢 Deployment Checklist

Before releasing:

- [ ] Code compiles without warnings
- [ ] All tests passing
- [ ] No hardcoded values
- [ ] ProGuard enabled
- [ ] Version bumped
- [ ] Changelog updated
- [ ] APK/AAB generated
- [ ] Screenshots prepared
- [ ] Play Store description ready
- [ ] Privacy policy reviewed

See `BUILD_GUIDE.md` for detailed steps.

---

## 💬 Need Help?

### Documentation
- 📖 `README.md` - Overview
- 🔒 `PRIVACY.md` - Privacy model
- 🏗️ `IMPLEMENTATION_SUMMARY.md` - Architecture
- 🤝 `CONTRIBUTING.md` - Guidelines
- 📋 `PROJECT_COMPLETION_REPORT.md` - Status

### Code Comments
- Most classes have KDoc comments
- Complex functions explained
- TODOs marked with dates

### Ask Questions
- Open GitHub Issue
- Create Discussion
- Email: dev@smartcallshield.com

---

## ✅ What's Next?

1. **Read the architecture**: Check `IMPLEMENTATION_SUMMARY.md`
2. **Explore the code**: Start with `MainActivity.kt`
3. **Run the tests**: Execute `./gradlew test`
4. **Make a change**: Try the example above
5. **Contribute**: Follow `CONTRIBUTING.md`

---

## 🎉 You're Ready!

You now have a fully functional, production-ready Android app.

**Happy coding!** 🚀

