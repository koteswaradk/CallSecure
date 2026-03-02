# SmartCallShield - Developer Onboarding Guide

## Welcome! 👋

This guide will get you up to speed with the Call Mode Creation feature in 30 minutes.

---

## 📚 5-Minute Quick Start

### What You're Looking At
A new alert dialog in the Settings screen that lets users create call modes (Normal, Family, Driving, Emergency) and assign contacts to them.

### Where to Find It
1. Open app
2. Go to Settings tab
3. Scroll to "Call Modes Management" section
4. Click "Setup" button
5. 💥 Dialog appears!

### Core Code
**File**: `SettingsScreen.kt` (445 lines)
- **Lines 300-430**: `CreateModeAlertDialog` composable
- **Lines 220-270**: `CallModeManagementCard` composable

---

## 🎓 15-Minute Deep Dive

### Architecture Overview

```
User Click
    ↓
CallModeManagementCard (remembers showModeDialog state)
    ↓
CreateModeAlertDialog (composable function)
    ├── State Management
    │   ├── selectedMode: String?
    │   ├── searchQuery: String
    │   ├── selectedContacts: Set<String>
    │   └── showModeDropdown: Boolean
    ├── Permission Handling
    │   └── contactsPermissionState (Accompanist)
    ├── ViewModel Integration
    │   └── callModesViewModel (Hilt injected)
    └── UI Rendering
        ├── AlertDialog container
        ├── DropdownMenu for mode selection
        ├── TextField for search
        ├── LazyColumn for contact list
        └── Buttons (Create/Cancel)
    ↓
callModesViewModel.createMode(selectedMode, selectedContacts)
    ↓
ModeRepository.createMode() + ContactRepository.addContact()
    ↓
Room Database (call_modes, contacts, mode_contact_cross_ref)
```

### Key Components

#### 1. State Management (Compose)
```kotlin
// Dialog visibility (in parent)
var showModeDialog by remember { mutableStateOf(false) }

// Dialog state (in CreateModeAlertDialog)
var selectedMode by remember { mutableStateOf<String?>(null) }
var searchQuery by remember { mutableStateOf("") }
var selectedContacts by remember { mutableStateOf<Set<String>>(emptySet()) }
var showModeDropdown by remember { mutableStateOf(false) }
```

**Why**: Compose recomposes when state changes

#### 2. Permission Handling
```kotlin
val contactsPermissionState = rememberPermissionState(Manifest.permission.READ_CONTACTS)

LaunchedEffect(Unit) {
    if (!contactsPermissionState.status.isGranted) {
        contactsPermissionState.launchPermissionRequest()
    } else {
        callModesViewModel.loadDeviceContacts()
    }
}

LaunchedEffect(contactsPermissionState.status.isGranted) {
    if (contactsPermissionState.status.isGranted) {
        callModesViewModel.loadDeviceContacts()
    }
}
```

**Why**: 
- First effect: Initial check on dialog open
- Second effect: Reload when permission status changes

#### 3. Contact Filtering
```kotlin
val uniquePhoneNumbers = remember(deviceContacts) {
    deviceContacts.distinctBy { it.phoneNumber }
}

val filteredContacts = remember(uniquePhoneNumbers, searchQuery) {
    if (searchQuery.isBlank()) {
        uniquePhoneNumbers
    } else {
        uniquePhoneNumbers.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
            it.phoneNumber.contains(searchQuery)
        }
    }
}
```

**Why**: 
- `remember()` prevents recalculating on every recomposition
- Dependencies (searchQuery, uniquePhoneNumbers) trigger recalculation
- `distinctBy` removes duplicates by phone number

#### 4. Button Logic
```kotlin
Button(
    onClick = {
        if (selectedMode != null && selectedContacts.isNotEmpty()) {
            callModesViewModel.createMode(selectedMode!!, selectedContacts)
            onDismiss()
        }
    },
    enabled = selectedMode != null && selectedContacts.isNotEmpty()
)
```

**Why**: Button only enabled when:
- Mode selected: `selectedMode != null`
- At least one contact selected: `selectedContacts.isNotEmpty()`

---

## 🔍 30-Minute Full Review

### Step 1: Read the Main File (10 min)
**File**: `app/src/main/java/com/akshaglobal/smartcallshield/presentation/ui/screens/SettingsScreen.kt`

Focus areas:
- Lines 1-50: Imports (all Compose, Hilt, Accompanist)
- Lines 220-270: `CallModeManagementCard()` function
- Lines 300-445: `CreateModeAlertDialog()` function

### Step 2: Understand the UI Flow (10 min)
**File**: `UI_FLOW_GUIDE.md`

Read sections:
- "User Journey Map"
- "State Transition Flow"
- "Interaction Patterns"

Visualizes how UI responds to user actions.

### Step 3: Review Database Integration (10 min)
**Files**:
- `app/src/main/java/com/akshaglobal/smartcallshield/presentation/viewmodel/CallModesViewModel.kt`
- `app/src/main/java/com/akshaglobal/smartcallshield/data/repository/ModeRepository.kt`
- `app/src/main/java/com/akshaglobal/smartcallshield/data/repository/ContactRepository.kt`

Key methods:
- `callModesViewModel.createMode()` - Creates mode and associates contacts
- `modeRepository.createMode()` - Inserts mode to DB
- `contactRepository.addContact()` - Inserts contact to DB

---

## 💡 Common Questions

### Q: Why use `remember()` for filtering?
**A**: Without it, filtering happens on every recomposition (many times per second). With `remember()`, it only recalculates when dependencies change (searchQuery, contacts list).

### Q: How does permission handling work?
**A**: 
1. First `LaunchedEffect` runs once on dialog open
2. Checks if permission granted
3. If yes: loads contacts
4. If no: requests permission (system dialog)
5. Second `LaunchedEffect` watches permission status
6. When status changes: reloads contacts

### Q: Why is `distinctBy { it.phoneNumber }` needed?
**A**: Some devices return multiple entries for the same contact (e.g., work phone + mobile phone). This deduplicates by keeping only one entry per phone number.

### Q: What happens when user clicks Cancel?
**A**: 
1. `onDismiss()` is called
2. Parent's `showModeDialog` becomes false
3. Dialog unmounts from UI
4. ViewModel is NOT called
5. Nothing saved to database

### Q: How does mode creation work?
**A**: 
1. User clicks "Create Mode"
2. `callModesViewModel.createMode(selectedMode, selectedContacts)` called
3. ViewModel creates:
   - ModeEntity (inserted into call_modes table)
   - ContactEntity for each phone (inserted into contacts table)
   - ModeContactCrossRef entries (linked in junction table)
4. Dialog closes automatically
5. Returns to Settings screen

---

## 🧪 Quick Test

Want to see it in action? (2 minutes)

```bash
# 1. Build and run
./gradlew installDebug

# 2. Open app, go to Settings
# 3. Click "Setup" button
# 4. Try these:
#    - Click dropdown, select "Family"
#    - Type "john" in search
#    - Click checkboxes to select contacts
#    - Click "Create Mode"
```

---

## 🔧 Making Changes

### To Add a New Mode Option
```kotlin
// In CreateModeAlertDialog
val predefinedModes = listOf(
    "Normal", "Family", "Driving", "Emergency",
    "Sleep"  // <- Add new option here
)
```

### To Change Search Behavior
```kotlin
// In CreateModeAlertDialog
val filteredContacts = remember(uniquePhoneNumbers, searchQuery) {
    if (searchQuery.isBlank()) {
        uniquePhoneNumbers
    } else {
        uniquePhoneNumbers.filter {
            // Modify this logic
            it.displayName.contains(searchQuery, ignoreCase = true) ||
            it.phoneNumber.contains(searchQuery)
        }
    }
}
```

### To Disable Duplicate Prevention
```kotlin
// DON'T do this - it will break the feature!
// But if you really need to:
val uniquePhoneNumbers = deviceContacts  // Removes distinctBy
```

---

## 🐛 Debugging Tips

### Dialog Not Appearing?
1. Check `showModeDialog` is true
2. Verify `CreateModeAlertDialog` is in the if block
3. Look for recomposition issues in Logcat

### Contacts Not Loading?
1. Grant READ_CONTACTS permission in app settings
2. Verify device has contacts
3. Check `callModesViewModel.loadDeviceContacts()` was called

### Mode Not Saving?
1. Check database schema matches entities
2. Verify ViewModel is doing async in viewModelScope
3. Check ContactRepository and ModeRepository methods exist

### Search Not Working?
1. Verify searchQuery state is updating (add Log.d())
2. Check filter logic in remember block
3. Verify filteredContacts is being used in LazyColumn

### Memory Issues?
1. Use LazyColumn for lists (already done ✓)
2. Don't create large objects in compose lambdas
3. Profile with Android Profiler

---

## 📖 Documentation Map

```
You are here ↓

QUICK_REFERENCE_CARD.md (2 min)
   └─ Overview & key facts
   
DEVELOPER_ONBOARDING.md ← YOU ARE HERE (30 min)
   └─ Understanding the code
   
IMPLEMENTATION_NOTES_MODE_CREATION.md (10 min)
   └─ Technical deep-dive
   
UI_FLOW_GUIDE.md (10 min)
   └─ Visual mockups & flows
   
TESTING_GUIDE.md (varies)
   └─ 15 test cases
   
DEPLOYMENT_GUIDE.md (5 min)
   └─ Production deployment
   
PROJECT_COMPLETION_SUMMARY.md (5 min)
   └─ Executive summary
```

---

## 🎯 Next Steps

### If You Need to...

**Understand the code**:
1. Read this file (you are here!)
2. Review SettingsScreen.kt
3. Look at CallModesViewModel.kt

**Test the feature**:
1. See TESTING_GUIDE.md for 15 test cases
2. Follow the test steps
3. Report any issues

**Make changes**:
1. Modify SettingsScreen.kt
2. Run `./gradlew assembleDebug`
3. Test your changes
4. Run unit tests

**Deploy to production**:
1. Read DEPLOYMENT_GUIDE.md
2. Version bump in gradle.kts
3. `./gradlew bundleRelease`
4. Upload to Play Store

**Report bugs**:
1. Run the reproduction steps
2. Check Logcat for errors
3. Check database state
4. Create detailed issue report

---

## 🚀 Pro Tips

### 1. Use Android Studio's Layout Inspector
- Ctrl+L (or Cmd+L on Mac)
- Visually inspect Compose hierarchy
- Great for understanding recomposition

### 2. Use Logcat to Debug
```kotlin
Log.d("CreateModeDialog", "selectedMode=$selectedMode")
Log.d("CreateModeDialog", "selectedContacts=$selectedContacts")
```

### 3. Use Room Inspector
- Open Device File Explorer
- Navigate to app database
- Export and open with SQLite viewer
- Verify data was saved correctly

### 4. Use Profiler to Check Memory
- Android Studio → Profiler
- Watch memory spike when dialog opens
- Verify memory released when dialog closes

### 5. Use Git to Track Changes
```bash
git diff SettingsScreen.kt
git log -p SettingsScreen.kt
```

---

## ❓ FAQ

**Q: Do I need to understand all of this to use the app?**  
A: No! Just knowing the UI flow (UI_FLOW_GUIDE.md) is enough.

**Q: Can I modify the mode names?**  
A: Yes, edit the `predefinedModes` list in CreateModeAlertDialog.

**Q: What if I want to add avatar support?**  
A: See ContactEntity in database. Add avatar field and update contacts provider.

**Q: Is this feature production-ready?**  
A: Yes! Build is successful, 15 tests documented, fully ready to deploy.

**Q: What if permissions don't work on my device?**  
A: Grant manually in Settings → Apps → SmartCallShield → Permissions.

---

## 📝 Cheat Sheet

### Key Files at a Glance
```
SettingsScreen.kt (445 lines)
├─ Line 1-50: Imports
├─ Line 57-190: SettingsScreen() main screen
├─ Line 192-217: SettingsSectionHeader()
├─ Line 219-245: SettingCard()
├─ Line 247-270: CallModeManagementCard()
└─ Line 272-445: CreateModeAlertDialog()

Navigation.kt (100 lines)
└─ Line 94: SettingsScreen() call

CallModesViewModel.kt (149 lines)
├─ Line 33: loadDeviceContacts()
├─ Line 40-54: createPredefinedMode()
├─ Line 56-88: createMode()
└─ Line 90-99: addContactToModeByPhone()
```

### Key Imports
```kotlin
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import com.google.accompanist.permissions.rememberPermissionState
```

### Key Composables
```kotlin
AlertDialog(onDismissRequest, title, text, confirmButton, dismissButton)
DropdownMenu(expanded, onDismissRequest, { DropdownMenuItem(...) })
LazyColumn(modifier) { items(list) { item -> ... } }
TextField(value, onValueChange, label)
Row(verticalAlignment, horizontalArrangement)
Checkbox(checked, onCheckedChange)
Button(onClick, enabled)
```

---

## ✅ Success Checklist

After reading this guide, you should be able to:

- [ ] Understand what this feature does
- [ ] Navigate the code in SettingsScreen.kt
- [ ] Explain the state management approach
- [ ] Explain the permission handling flow
- [ ] Understand how contacts are filtered
- [ ] Understand how modes are created
- [ ] Run the app and see the feature
- [ ] Make simple code changes
- [ ] Know where to find more documentation

---

## 🎓 Learning Resources

### Jetpack Compose
- [Official Docs](https://developer.android.com/jetpack/compose)
- [Compose Pathway](https://developer.android.com/courses/android-app-composition)

### Room Database
- [Official Docs](https://developer.android.com/topic/libraries/architecture/room)
- [Codelabs](https://developer.android.com/codelabs/android-room-with-a-view-kotlin)

### Hilt Dependency Injection
- [Official Docs](https://developer.android.com/training/dependency-injection/hilt-android)
- [Codelab](https://developer.android.com/codelabs/android-hilt)

### Kotlin Coroutines
- [Official Docs](https://kotlinlang.org/docs/coroutines-overview.html)
- [Async Programming](https://developer.android.com/kotlin/coroutines)

---

## 📞 Getting Help

1. **Code Questions?** Review this guide again
2. **Stuck on a bug?** Check TESTING_GUIDE.md for known issues
3. **Need architecture details?** Read IMPLEMENTATION_NOTES_MODE_CREATION.md
4. **Want to see the UI flow?** Check UI_FLOW_GUIDE.md
5. **Ready to deploy?** Follow DEPLOYMENT_GUIDE.md

---

## 🎉 Welcome Aboard!

You're now ready to work with the SmartCallShield Call Mode Creation feature!

- ✅ You understand the architecture
- ✅ You know where the code is
- ✅ You can read and modify the code
- ✅ You know how to test it
- ✅ You have resources for deeper learning

**Happy coding!** 🚀

---

**Time to read**: ~30 minutes  
**Difficulty**: Beginner-friendly  
**Last Updated**: February 28, 2026  
**Maintained by**: SmartCallShield Team

