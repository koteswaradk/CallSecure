# Call Mode Creation Feature - Testing Guide

## Overview
This guide provides comprehensive testing steps for the Call Mode Creation feature implemented in the SettingsScreen.

## Pre-Testing Setup

### Environment Requirements
- Android API Level: 24+ (READ_CONTACTS permission available)
- Device/Emulator: Configured with test contacts
- Build: Debug or Release APK

### Test Device Preparation
1. **Create Test Contacts** on device:
   - Alice Johnson: +1-234-567-8901
   - Bob Smith: +1-345-678-9012
   - Carol Davis: +1-456-789-0123
   - David Wilson: +1-567-890-1234
   - Eve Martinez: +1-678-901-2345
   - (Minimum 5 contacts for comprehensive testing)

2. **Grant Permissions** (if not automatic):
   - Open Settings → Apps → CallSecure → Permissions
   - Enable "Contacts"

3. **Fresh Database** (optional but recommended):
   - Clear app data before testing
   - Reinstall if needed

---

## Test Cases

### TC-001: Dialog Accessibility
**Objective**: Verify dialog opens when Setup button is clicked

**Steps**:
1. Open CallSecure app
2. Navigate to Settings tab
3. Scroll to "Call Modes Management" section
4. Observe "Setup" button in the card
5. Click "Setup" button

**Expected Result**:
- ✅ Alert dialog appears immediately
- ✅ Dialog title: "Create & Manage Call Modes"
- ✅ Dialog contains mode dropdown and other controls
- ✅ Dialog background is semi-transparent
- ✅ Clicking outside doesn't close dialog (use Cancel button)

**Notes**:
- Dialog should appear with Compose's default animation
- Should work on different screen sizes (test in landscape/portrait)

---

### TC-002: Mode Dropdown Functionality
**Objective**: Verify mode selection dropdown works correctly

**Steps**:
1. Open Call Modes dialog
2. Observe initial state: "Choose a mode" text visible
3. Click dropdown button
4. Verify all 4 modes appear

**Expected Result**:
- ✅ Initial text: "Choose a mode"
- ✅ Dropdown menu appears with these options:
  - Normal
  - Family
  - Driving
  - Emergency
- ✅ Each option is clickable
- ✅ Visual feedback (ripple effect) on click

**Test Each Mode**:
- [ ] Click "Normal"
  - Selected text changes to "Normal"
  - Search bar appears below
  - Dropdown closes
- [ ] Click "Family"
  - Selected text changes to "Family"
  - Search bar appears below
  - Dropdown closes
- [ ] Click "Driving"
  - Selected text changes to "Driving"
  - Search bar appears below
  - Dropdown closes
- [ ] Click "Emergency"
  - Selected text changes to "Emergency"
  - Search bar appears below
  - Dropdown closes

**Notes**:
- Mode selection should stick (not revert on re-opening dropdown)
- Dropdown should close after selection

---

### TC-003: Search Functionality
**Objective**: Verify search bar filters contacts correctly

**Prerequisites**:
- Dialog open with mode selected

**Test Case 3A: Search by Name**:
1. Type "alice" in search field
2. Observe contact list

**Expected Result**:
- ✅ Only "Alice Johnson" visible
- ✅ Other contacts hidden
- ✅ Matches case-insensitive (type "ALICE", "alice", "Alice" all work)

**Test Case 3B: Search by Phone Number**:
1. Clear search field
2. Type "234" in search field
3. Observe contact list

**Expected Result**:
- ✅ Contacts with "234" in number visible
- ✅ (e.g., Alice with +1-234-567-8901)
- ✅ Other contacts hidden

**Test Case 3C: Partial Match**:
1. Clear search field
2. Type "mar" in search field
3. Observe contact list

**Expected Result**:
- ✅ "Eve Martinez" visible (contains "mar")
- ✅ Other contacts hidden

**Test Case 3D: Clear Search**:
1. Clear search field completely
2. Observe contact list

**Expected Result**:
- ✅ All contacts visible again
- ✅ Full list restored

**Test Case 3E: No Results**:
1. Type "xyz999" in search field
2. Observe contact list

**Expected Result**:
- ✅ No contacts displayed
- ✅ List is empty (no error message needed)

---

### TC-004: Duplicate Contact Handling
**Objective**: Verify duplicate contacts don't appear in list

**Setup**:
- Create a contact with multiple phone numbers:
  - "Alice Johnson"
    - Phone 1: +1-234-567-8901
    - Phone 2: +1-234-567-8902

**Steps**:
1. Open Call Modes dialog
2. Select a mode
3. Observe contact list

**Expected Result**:
- ✅ "Alice Johnson" appears ONCE
- ✅ NOT duplicated for each phone number
- ✅ When selected, uses the first matching phone number

**Notes**:
- CallSecure de-duplicates by phone number automatically
- Test with multiple duplicate scenarios if possible

---

### TC-005: Contact Multi-Selection
**Objective**: Verify checkbox selection works correctly

**Prerequisites**:
- Dialog open with mode selected
- Multiple contacts visible

**Test Case 5A: Single Selection**:
1. Click checkbox for "Alice Johnson"
2. Observe counter and button state

**Expected Result**:
- ✅ Checkbox becomes checked (✓)
- ✅ Counter shows "Select Contacts (1 selected)"
- ✅ "Create Mode" button becomes enabled

**Test Case 5B: Multiple Selection**:
1. Click checkboxes for "Alice Johnson", "Bob Smith", "Carol Davis"
2. Observe counter and button state

**Expected Result**:
- ✅ All three checkboxes checked
- ✅ Counter shows "Select Contacts (3 selected)"
- ✅ "Create Mode" button enabled

**Test Case 5C: Deselection**:
1. With 3 contacts selected, click "Bob Smith" checkbox again
2. Observe counter and button state

**Expected Result**:
- ✅ Bob Smith checkbox unchecked
- ✅ Counter shows "Select Contacts (2 selected)"
- ✅ "Create Mode" button still enabled (has other contacts)

**Test Case 5D: Select All Then Deselect All**:
1. Select all visible contacts
2. Deselect all contacts
3. Observe counter and button state

**Expected Result**:
- ✅ All checkboxes toggle correctly
- ✅ Counter shows "Select Contacts (0 selected)" when empty
- ✅ "Create Mode" button becomes disabled

**Notes**:
- Checkbox should have visual feedback (ripple effect)
- Selection persists when scrolling through LazyColumn

---

### TC-006: Create Mode Button Logic
**Objective**: Verify button is enabled/disabled correctly

**Test Case 6A: Initial State**:
1. Open dialog
2. Don't select mode or contacts

**Expected Result**:
- ✅ "Create Mode" button is DISABLED (grayed out)
- ✅ Cannot click button

**Test Case 6B: Mode Selected Only**:
1. Select a mode (e.g., "Family")
2. Don't select any contacts

**Expected Result**:
- ✅ "Create Mode" button is DISABLED
- ✅ Cannot click button
- ✅ Contact list visible

**Test Case 6C: Contacts Selected Only**:
1. Don't select a mode
2. Try to select contacts

**Expected Result**:
- ✅ Search bar doesn't appear (can't select without mode)
- ✅ "Create Mode" button is DISABLED

**Test Case 6D: Both Selected**:
1. Select mode (e.g., "Driving")
2. Select at least one contact (e.g., "Alice Johnson")

**Expected Result**:
- ✅ "Create Mode" button is ENABLED (bright color)
- ✅ Button is clickable
- ✅ Text is readable

**Notes**:
- Button state should update immediately
- No lag in state transitions

---

### TC-007: Mode Creation & Database Storage
**Objective**: Verify mode is created and stored correctly

**Prerequisites**:
- Database inspector tool installed (Room Inspector, Android Studio)

**Steps**:
1. Select mode: "Family"
2. Select 2 contacts: "Alice Johnson", "Carol Davis"
3. Click "Create Mode" button
4. Observe dialog closes

**Expected Result**:
- ✅ Dialog closes immediately
- ✅ Returns to Settings screen
- ✅ No error message

**Database Verification**:
1. Open Android Studio Device File Explorer
2. Navigate to: `data/data/com.akshaglobal.smartcallshield/databases/`
3. Pull database file
4. Open with SQLite viewer

**Expected Database State**:
- ✅ `call_modes` table has new "Family" entry
  - name: "Family"
  - isActive: false (or true, depends on default)
  - createdAt: current timestamp
- ✅ `contacts` table has entries for selected contacts
  - Alice Johnson with normalized phone
  - Carol Davis with normalized phone
- ✅ `mode_contact_cross_ref` has 2 entries linking:
  - Family mode ↔ Alice contact
  - Family mode ↔ Carol contact

---

### TC-008: Multiple Mode Creation
**Objective**: Verify multiple modes can be created

**Steps**:
1. Create mode "Family" with contacts: Alice, Bob
2. Click Settings → Setup again
3. Create mode "Emergency" with contacts: David, Eve
4. Verify both exist in database

**Expected Result**:
- ✅ Both modes created successfully
- ✅ No conflicts or overwrites
- ✅ Both appear in database with correct associations

---

### TC-009: Permission Request Flow
**Objective**: Verify permission handling

**Setup**:
- Uninstall and reinstall app (clears permissions)
- OR: Revoke READ_CONTACTS permission in app settings

**Steps**:
1. Open Settings
2. Click "Setup" button
3. When permission dialog appears, DENY it
4. Verify behavior
5. Go back and click "Setup" again
6. Grant permission this time
7. Verify contacts load

**Expected Result**:
- ✅ Permission request dialog appears (system dialog)
- ✅ When denied: Contact list empty or shows placeholder
- ✅ When granted: Contacts load immediately
- ✅ No crashes or errors

**Notes**:
- First LaunchedEffect checks permission
- Second LaunchedEffect reloads when status changes

---

### TC-010: Scroll Performance
**Objective**: Verify LazyColumn renders efficiently with large contact lists

**Setup**:
- Create 50+ contacts on test device

**Steps**:
1. Open Call Modes dialog
2. Select a mode
3. Scroll through entire contact list
4. Search to filter list
5. Scroll filtered list

**Expected Result**:
- ✅ Smooth scrolling (no jank)
- ✅ No UI freezes
- ✅ Contacts render on-demand
- ✅ Search results update smoothly

**Performance Metrics**:
- Frame rate should remain ~60 FPS
- No memory leaks during scrolling
- App doesn't crash with large lists

---

### TC-011: Text Input Edge Cases
**Objective**: Verify search handles edge cases

**Test Cases**:
1. Type special characters: "@#$%^&*()"
   - Should filter normally or show no results
   - No crash

2. Type very long text (100+ characters)
   - Should handle gracefully
   - Text field doesn't overflow

3. Type unicode/emoji: "🔥📱"
   - Should handle or display safely
   - No crash

4. Type spaces and tabs
   - Should filter correctly
   - No unexpected behavior

**Expected Result**:
- ✅ All cases handled without crash
- ✅ App remains responsive

---

### TC-012: Cancel Button
**Objective**: Verify Cancel button closes dialog without saving

**Steps**:
1. Open dialog
2. Select mode and contacts
3. Click "Cancel" button
4. Check database

**Expected Result**:
- ✅ Dialog closes immediately
- ✅ No mode created in database
- ✅ No error messages
- ✅ Returns to Settings screen

---

### TC-013: Orientation Change
**Objective**: Verify dialog survives orientation change

**Steps**:
1. Open dialog with mode and contacts selected
2. Rotate device (portrait → landscape)
3. Verify dialog state

**Expected Result**:
- ✅ Dialog remains open
- ✅ All selections preserved
- ✅ Layout adjusts properly
- ✅ No data loss

---

### TC-014: Back Button Behavior
**Objective**: Verify system back button behavior

**Steps**:
1. Open dialog
2. Press system back button (or gesture)

**Expected Result**:
- ✅ Dialog closes (onDismissRequest triggered)
- ✅ Equivalent to "Cancel" button
- ✅ No mode created
- ✅ Returns to Settings

---

### TC-015: Memory & Resource Management
**Objective**: Verify no memory leaks

**Steps**:
1. Open and close dialog 10+ times
2. Use Android Profiler to monitor memory
3. Check for leaks

**Expected Result**:
- ✅ Memory released after dialog closes
- ✅ No growing memory usage
- ✅ No resource leaks

---

## Regression Testing Checklist

After making any changes, test these critical paths:

- [ ] Settings screen loads without crash
- [ ] Setup button visible and clickable
- [ ] Dialog opens and closes
- [ ] Mode dropdown shows all 4 options
- [ ] Search filters contacts correctly
- [ ] Contacts can be selected/deselected
- [ ] Create button enabled/disabled logic works
- [ ] Mode successfully created in database
- [ ] No duplicate contacts in list
- [ ] Permissions handled correctly

---

## Known Issues & Workarounds

### Issue: Dropdown extends outside dialog
**Status**: Known limitation
**Workaround**: Adjust device font size or dialog width

### Issue: Long contact names truncate
**Status**: Expected behavior
**Workaround**: Trim in UI or use ellipsis

### Issue: Permission dialog doesn't appear
**Status**: Usually system issue
**Workaround**: Check device settings, reinstall app

---

## Performance Targets

| Metric | Target | Actual |
|--------|--------|--------|
| Dialog open time | <100ms | __ |
| Contact load time | <500ms | __ |
| Search response | <200ms | __ |
| Mode creation | <1s | __ |
| Memory usage | <50MB | __ |
| Frame rate | 60 FPS | __ |

---

## Test Results Log

**Date**: ______________
**Tester**: ______________
**Build**: ______________
**Device**: ______________

| TC # | Test Name | Status | Notes |
|------|-----------|--------|-------|
| 001 | Dialog Accessibility | ☐ Pass ☐ Fail | |
| 002 | Mode Dropdown | ☐ Pass ☐ Fail | |
| 003 | Search Functionality | ☐ Pass ☐ Fail | |
| 004 | Duplicate Handling | ☐ Pass ☐ Fail | |
| 005 | Multi-Selection | ☐ Pass ☐ Fail | |
| 006 | Button Logic | ☐ Pass ☐ Fail | |
| 007 | Database Storage | ☐ Pass ☐ Fail | |
| 008 | Multiple Modes | ☐ Pass ☐ Fail | |
| 009 | Permissions | ☐ Pass ☐ Fail | |
| 010 | Scroll Performance | ☐ Pass ☐ Fail | |
| 011 | Edge Cases | ☐ Pass ☐ Fail | |
| 012 | Cancel Button | ☐ Pass ☐ Fail | |
| 013 | Orientation | ☐ Pass ☐ Fail | |
| 014 | Back Button | ☐ Pass ☐ Fail | |
| 015 | Memory | ☐ Pass ☐ Fail | |

**Summary**: _____ passed, _____ failed

**Sign-off**: __________________ Date: __________

---

**Last Updated**: February 28, 2026
**Version**: 1.0

