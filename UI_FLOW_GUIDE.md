# Call Mode Creation UI Flow - Visual Guide

## User Journey Map

```
┌─────────────────────────────────────────────────────────────┐
│                    SETTINGS SCREEN                           │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ Settings                                                  │ │
│  │                                                            │ │
│  │ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │ │
│  │ Spam Detection                                            │ │
│  │   ☑ Enable Spam Detection                                │ │
│  │   ☑ Auto-Reject Spam                                     │ │
│  │   Confidence Threshold: 75.0%  [════════┤] │ │
│  │                                                            │ │
│  │ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │ │
│  │ Driving Mode                                              │ │
│  │   ☑ Enable Driving Mode                                  │ │
│  │   Auto-Reply Message: [________________]                 │ │
│  │                                                            │ │
│  │ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │ │
│  │ Call Modes Management                                     │ │
│  │ ┌─────────────────────────────────────────────────────┐  │ │
│  │ │ Create & Manage Modes                             │  │ │
│  │ │ Setup Normal, Family, Driving, Emergency modes    │  │ │
│  │ │                              [ Setup > ]            │  │ │
│  │ └─────────────────────────────────────────────────────┘  │ │
│  │                                                            │ │
│  │ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │ │
│  │ Privacy & Data                                            │ │
│  │   ☑ Analytics                                            │ │
│  │   ☐ Cloud Sync (Premium)                                │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              ↓
                    USER CLICKS "Setup" BUTTON
                              ↓
```

## Alert Dialog - Initial State (No Mode Selected)

```
╔═════════════════════════════════════════════════════════════╗
║ Create & Manage Call Modes                             [✕]  ║
╠═════════════════════════════════════════════════════════════╣
║                                                              ║
║  Select Call Mode                                            ║
║  ┌──────────────────────────────────────────────────────┐   ║
║  │ Choose a mode                              ▼          │   ║
║  └──────────────────────────────────────────────────────┘   ║
║                                                              ║
║  (Search contacts section hidden)                            ║
║                                                              ║
╠═════════════════════════════════════════════════════════════╣
║                 [ Create Mode ]   [ Cancel ]                ║
║                   (disabled)                                ║
╚═════════════════════════════════════════════════════════════╝
```

## Alert Dialog - Mode Dropdown Expanded

```
╔═════════════════════════════════════════════════════════════╗
║ Create & Manage Call Modes                             [✕]  ║
╠═════════════════════════════════════════════════════════════╣
║                                                              ║
║  Select Call Mode                                            ║
║  ┌──────────────────────────────────────────────────────┐   ║
║  │ Choose a mode                              ▼          │   ║
║  ├──────────────────────────────────────────────────────┤   ║
║  │ ○ Normal                                             │   ║
║  │ ○ Family                                             │   ║
║  │ ○ Driving                                            │   ║
║  │ ○ Emergency                                          │   ║
║  └──────────────────────────────────────────────────────┘   ║
║                                                              ║
║  (Search contacts section hidden)                            ║
║                                                              ║
╠═════════════════════════════════════════════════════════════╣
║                 [ Create Mode ]   [ Cancel ]                ║
║                   (disabled)                                ║
╚═════════════════════════════════════════════════════════════╝
```

## Alert Dialog - Mode Selected, Search Bar Visible

```
╔═════════════════════════════════════════════════════════════╗
║ Create & Manage Call Modes                             [✕]  ║
╠═════════════════════════════════════════════════════════════╣
║                                                              ║
║  Select Call Mode                                            ║
║  ┌──────────────────────────────────────────────────────┐   ║
║  │ Family                                     ▼          │   ║
║  └──────────────────────────────────────────────────────┘   ║
║                                                              ║
║  Search Contacts                                             ║
║  ┌──────────────────────────────────────────────────────┐   ║
║  │ [Search by name or number]                           │   ║
║  └──────────────────────────────────────────────────────┘   ║
║                                                              ║
║  Select Contacts (0 selected)                                ║
║  ┌──────────────────────────────────────────────────────┐   ║
║  │ ┌────────────────────────────────────────────────┐   │   ║
║  │ │ Alice Johnson                            ☐      │   │   ║
║  │ │ +1-234-567-8901                                │   │   ║
║  │ ├────────────────────────────────────────────────┤   │   ║
║  │ │ Bob Smith                                  ☐      │   │   ║
║  │ │ +1-345-678-9012                                │   │   ║
║  │ ├────────────────────────────────────────────────┤   │   ║
║  │ │ Carol Davis                                ☐      │   │   ║
║  │ │ +1-456-789-0123                                │   │   ║
║  │ ├────────────────────────────────────────────────┤   │   ║
║  │ │ (scrollable list...)                           │   │   ║
║  │ └────────────────────────────────────────────────┘   │   ║
║  └──────────────────────────────────────────────────────┘   ║
║                                                              ║
╠═════════════════════════════════════════════════════════════╣
║                 [ Create Mode ]   [ Cancel ]                ║
║                   (disabled)                                ║
╚═════════════════════════════════════════════════════════════╝
```

## Alert Dialog - After Contact Selection

```
╔═════════════════════════════════════════════════════════════╗
║ Create & Manage Call Modes                             [✕]  ║
╠═════════════════════════════════════════════════════════════╣
║                                                              ║
║  Select Call Mode                                            ║
║  ┌──────────────────────────────────────────────────────┐   ║
║  │ Family                                     ▼          │   ║
║  └──────────────────────────────────────────────────────┘   ║
║                                                              ║
║  Search Contacts                                             ║
║  ┌──────────────────────────────────────────────────────┐   ║
║  │ [Search by name or number]                           │   ║
║  └──────────────────────────────────────────────────────┘   ║
║                                                              ║
║  Select Contacts (2 selected)  ← COUNT UPDATED              ║
║  ┌──────────────────────────────────────────────────────┐   ║
║  │ ┌────────────────────────────────────────────────┐   │   ║
║  │ │ Alice Johnson                            ☑      │   │   ║
║  │ │ +1-234-567-8901                                │   │   ║
║  │ ├────────────────────────────────────────────────┤   │   ║
║  │ │ Bob Smith                                  ☐      │   │   ║
║  │ │ +1-345-678-9012                                │   │   ║
║  │ ├────────────────────────────────────────────────┤   │   ║
║  │ │ Carol Davis                                ☑      │   │   ║
║  │ │ +1-456-789-0123                                │   │   ║
║  │ ├────────────────────────────────────────────────┤   │   ║
║  │ │ (scrollable list...)                           │   │   ║
║  │ └────────────────────────────────────────────────┘   │   ║
║  └──────────────────────────────────────────────────────┘   ║
║                                                              ║
╠═════════════════════════════════════════════════════════════╣
║                 [ Create Mode ]   [ Cancel ]                ║
║                   (enabled!)   ← BUTTON ENABLED             ║
╚═════════════════════════════════════════════════════════════╝
```

## Alert Dialog - Search Filter Active

```
╔═════════════════════════════════════════════════════════════╗
║ Create & Manage Call Modes                             [✕]  ║
╠═════════════════════════════════════════════════════════════╣
║                                                              ║
║  Select Call Mode                                            ║
║  ┌──────────────────────────────────────────────────────┐   ║
║  │ Family                                     ▼          │   ║
║  └──────────────────────────────────────────────────────┘   ║
║                                                              ║
║  Search Contacts                                             ║
║  ┌──────────────────────────────────────────────────────┐   ║
║  │ [alice]                              ✕               │   ║
║  └──────────────────────────────────────────────────────┘   ║
║                 ↓ Filtered results shown                     ║
║  Select Contacts (1 selected)                                ║
║  ┌──────────────────────────────────────────────────────┐   ║
║  │ ┌────────────────────────────────────────────────┐   │   ║
║  │ │ Alice Johnson                            ☑      │   │   ║
║  │ │ +1-234-567-8901                                │   │   ║
║  │ └────────────────────────────────────────────────┘   │   ║
║  │                                                      │   ║
║  │ (Only matching results shown)                       │   ║
║  └──────────────────────────────────────────────────────┘   ║
║                                                              ║
╠═════════════════════════════════════════════════════════════╣
║                 [ Create Mode ]   [ Cancel ]                ║
║                   (enabled!)                                ║
╚═════════════════════════════════════════════════════════════╝
```

## State Transition Flow

```
                    ┌──────────────┐
                    │ Dialog Closed │
                    └──────────────┘
                          ↑
                          │ onDismiss()
                          │
          ┌───────────────┴───────────────┐
          │ INITIAL STATE                 │
          │ • selectedMode = null         │
          │ • searchQuery = ""            │
          │ • selectedContacts = {}       │
          │ • showModeDropdown = false    │
          └───────────────┬───────────────┘
                          │
                          ↓
          ┌───────────────────────────────┐
          │ User clicks dropdown button    │
          └───────────────┬───────────────┘
                          │
                          ↓
          ┌───────────────────────────────┐
          │ DROPDOWN OPEN                 │
          │ • showModeDropdown = true     │
          └───────────────┬───────────────┘
                          │
                          ↓
          ┌───────────────────────────────┐
          │ User selects mode             │
          └───────────────┬───────────────┘
                          │
                          ↓
          ┌───────────────────────────────┐
          │ MODE SELECTED                 │
          │ • selectedMode = "Family"     │
          │ • showModeDropdown = false    │
          │ • Search visible              │
          └───────────────┬───────────────┘
                          │
                          ↓
     ┌────────────────────────────────────────────┐
     │ User types in search (optional)             │
     │ • searchQuery updated in real-time         │
     │ • filteredContacts recalculated            │
     └────────────────────┬───────────────────────┘
                          │
                          ↓
     ┌────────────────────────────────────────────┐
     │ User selects contacts via checkboxes       │
     │ • selectedContacts updated                 │
     │ • Selection counter increases              │
     │ • Create button becomes enabled            │
     └────────────────────┬───────────────────────┘
                          │
                          ↓
     ┌────────────────────────────────────────────┐
     │ User clicks "Create Mode"                  │
     │ • ViewModel.createMode() called            │
     │ • Mode stored to database                  │
     │ • onDismiss() triggered                    │
     └────────────────────┬───────────────────────┘
                          │
                          ↓
                    ┌──────────────┐
                    │ Dialog Closed │
                    │ Back to       │
                    │ Settings      │
                    └──────────────┘
```

## Interaction Patterns

### 1. Mode Selection
```
Click Dropdown Button
    ↓
showModeDropdown = true
    ↓
DropdownMenu appears (with animation)
    ↓
User clicks option
    ↓
selectedMode = "Family"
showModeDropdown = false
    ↓
Dropdown closes, Search bar appears
```

### 2. Contact Search
```
User types in search field
    ↓
searchQuery state updated
    ↓
remember(uniquePhoneNumbers, searchQuery) recalculates
    ↓
filteredContacts = filter by name/number
    ↓
LazyColumn re-renders with filtered items
```

### 3. Contact Selection
```
User clicks checkbox
    ↓
onCheckedChange { isSelected ->
    ↓
if (isSelected)
    selectedContacts += phoneNumber
else
    selectedContacts -= phoneNumber
    ↓
Selection counter updates
Create button enabled/disabled based on logic
```

## Permission Flow

```
┌────────────────────────────────────┐
│ CreateModeAlertDialog Appears       │
└────────────────┬───────────────────┘
                 │
                 ↓
         LaunchedEffect(Unit)
                 │
                 ↓
    CHECK: contactsPermissionState.status.isGranted?
                 │
        ┌────────┴────────┐
        ↓                 ↓
      YES               NO
        │                 │
        │         requestPermission()
        │                 │
        └────────┬────────┘
                 │
                 ↓
         LaunchedEffect(isGranted)
                 │
                 ↓
    callModesViewModel.loadDeviceContacts()
                 │
                 ↓
         Contacts displayed
```

## Empty State Handling

### No Contacts Available
```
╔═════════════════════════════════════════╗
║ Create & Manage Call Modes         [✕]  ║
╠═════════════════════════════════════════╣
║                                          ║
║  Select Call Mode                        ║
║  ┌──────────────────────────────────┐   ║
║  │ Family                    ▼      │   ║
║  └──────────────────────────────────┘   ║
║                                          ║
║  Search Contacts                         ║
║  ┌──────────────────────────────────┐   ║
║  │ [Search by name or number]       │   ║
║  └──────────────────────────────────┘   ║
║                                          ║
║  Select Contacts (0 selected)            ║
║  ┌──────────────────────────────────┐   ║
║  │                                   │   ║
║  │ (No contacts available)           │   ║
║  │                                   │   ║
║  └──────────────────────────────────┘   ║
║                                          ║
╠═════════════════════════════════════════╣
║             [ Create Mode ]  [ Cancel ]  ║
║               (disabled)                 ║
╚═════════════════════════════════════════╝

→ Possible causes:
  • Device has no contacts
  • Permission not granted
  • All contacts filtered by search
```

### Permission Denied
```
╔═════════════════════════════════════════╗
║ Create & Manage Call Modes         [✕]  ║
╠═════════════════════════════════════════╣
║                                          ║
║  Select Call Mode                        ║
║  ┌──────────────────────────────────┐   ║
║  │ Family                    ▼      │   ║
║  └──────────────────────────────────┘   ║
║                                          ║
║  Search Contacts                         ║
║  ┌──────────────────────────────────┐   ║
║  │ [Search by name or number]       │   ║
║  └──────────────────────────────────┘   ║
║                                          ║
║  Select Contacts (0 selected)            ║
║  ┌──────────────────────────────────┐   ║
║  │                                   │   ║
║  │ Permission required to access     │   ║
║  │ device contacts.                  │   ║
║  │ [Request Permission]              │   ║
║  │                                   │   ║
║  └──────────────────────────────────┘   ║
║                                          ║
╠═════════════════════════════════════════╣
║             [ Create Mode ]  [ Cancel ]  ║
║               (disabled)                 ║
╚═════════════════════════════════════════╝
```

## Key Behaviors Summary

| Action | Condition | Result |
|--------|-----------|--------|
| Show Dialog | "Setup" clicked | Dialog appears with focus |
| Show Dropdown | Button clicked | Options visible |
| Select Mode | Option clicked | Search bar appears |
| Filter Search | Text entered | Contact list updates |
| Toggle Checkbox | Clicked | Selection state toggles |
| Create Button | Mode + Contacts | Becomes enabled |
| Create Mode | Button clicked | Mode saved, dialog closes |
| Cancel | Button clicked | Dialog closes (no save) |
| Load Contacts | Permission granted | List populates |

---

**Interactive Elements**: 7 (Dropdown, TextField, Checkboxes x3+, Buttons x2)
**States**: 8+ (Initial, Dropdown Open, Mode Selected, Searching, Selecting, Permission Denied, etc.)
**Animations**: Dialog appearance, dropdown expansion (system defaults)

