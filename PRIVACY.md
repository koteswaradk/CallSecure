# SmartCallShield Privacy Policy

**Effective Date:** January 2025  
**Last Updated:** January 2025

## 📌 Overview

SmartCallShield is committed to protecting your privacy. This privacy policy explains how we handle your data and respect your privacy while providing a secure, intelligent call management service.

**Core Principle:** Your data stays on your device.

---

## 1. Data We Collect

### 1.1 Information Collected Automatically

When you use SmartCallShield, we collect:

**On Your Device (Local Storage Only):**
- Incoming/outgoing call logs (phone numbers, timestamps, duration)
- Your contacts list (names, phone numbers, categories)
- SMS messages you send via auto-reply features
- App settings and preferences
- Call decision logs (whether calls were blocked/allowed)

**These are stored locally in an encrypted SQLite database.**

### 1.2 Information You Provide

- Contact lists (whitelist, blacklist, family, emergency)
- Custom auto-reply messages
- AI model preferences and thresholds
- Optional feedback and crash reports

### 1.3 What We Do NOT Collect

❌ We never collect:
- Your audio (calls are never recorded)
- Transcripts of conversations
- Location data (unless you enable GPS-based driving detection)
- Contacts you don't explicitly manage in the app
- Device identifiers or unique IDs
- Analytics data (unless you opt-in)

---

## 2. How We Use Your Data

### 2.1 Primary Uses

| Data | Purpose | Where Stored |
|------|---------|--------------|
| Call logs | Spam detection, blocking decisions | Local device only |
| Contacts | Whitelist matching, classification | Local device only |
| Settings | Customize your experience | Local device + Optional cloud |
| SMS auto-replies | Respond to calls in driving mode | Local device only |
| AI features | Train local model (never cloud) | Device memory only |

### 2.2 AI Model Processing

Our spam detection works entirely on your device:

```
Your Phone Number
       ↓
Local Feature Extraction (on-device)
       ↓
TensorFlow Lite Model (on-device)
       ↓
Spam Score (99.9% kept local)
       ↓
Decision: Allow/Block/Silent
```

**No data is sent to our servers for spam detection.**

### 2.3 Optional Analytics (Premium)

With permission, we collect anonymized:
- **Daily statistics** (counts, no numbers): 5 calls, 2 spam, 1 driving reply
- **Aggregate trends**: Peak calling hours
- **Feature usage**: Which modes are used

**Even with analytics enabled:**
- ✅ Phone numbers are NEVER sent
- ✅ Contact information is NEVER sent
- ✅ Call content is NEVER sent
- ✅ You can disable at any time

---

## 3. Data Storage & Security

### 3.1 Local Storage (Device)

All data is encrypted using Android's built-in security:

```
SQLite Database
├─ Encrypted at rest
├─ Protected by device lock
├─ Requires app authentication
└─ Isolated from other apps
```

**Encryption Standard:** AES-256 (Android KeyStore)

### 3.2 Cloud Storage (Premium Feature - Optional)

For users who enable cloud sync:
- End-to-end encrypted backup
- Only your encrypted data on servers
- Keys never leave your device
- Optional, can be disabled anytime

**Cloud Provider:** Secure infrastructure (Firebase/AWS)

### 3.3 Backup Handling

**Google Drive Backup (Android Feature):**
- May include app data if system backup is enabled
- Controlled by Android settings, not our app
- Disable in: Settings → Google → Manage your Google account → Backups

---

## 4. Data Retention

### 4.1 Auto-Deletion Policies

| Data Type | Retention Period | Why |
|-----------|-----------------|-----|
| Call logs | 30 days (configurable) | Storage efficiency |
| Spam reports | 90 days | Pattern analysis |
| Driving mode logs | 30 days | Historical reference |
| Contacts | Until deleted manually | User control |
| Settings | Indefinite | User preference |

### 4.2 Manual Deletion

Users can:
- Delete individual call logs
- Clear all data in Settings
- Remove contacts at any time
- Reset app to factory defaults

---

## 5. Permissions Explained

### 5.1 Required Permissions

**READ_CALL_LOG**
- Used for: Detecting call patterns, blocking decisions
- Shared with: Only local database
- Why needed: Core feature

**READ_PHONE_STATE**
- Used for: Detecting incoming calls in real-time
- Shared with: In-memory processing only
- Why needed: Intercept spam calls

**SEND_SMS**
- Used for: Sending auto-reply messages in driving mode
- Shared with: SMS Manager (system)
- Why needed: Required for auto-reply feature

**READ_CONTACTS**
- Used for: Matching contacts to phone numbers
- Shared with: Local database only
- Why needed: Whitelist matching

**CALL_PHONE**
- Used for: Initiating calls (future feature)
- Shared with: System dialer
- Why needed: Callback feature

**WRITE_CALL_LOG**
- Used for: Logging blocked calls
- Shared with: System call log
- Why needed: System integration

### 5.2 Optional Permissions

**ACCESS_FINE_LOCATION** (GPS)
- Used for: Auto-detecting driving via vehicle speed
- Shared with: Local device memory only
- Privacy: Processed immediately, not stored
- Can be: Disabled in Settings

**INTERNET**
- Used for: Cloud sync (premium only)
- Shared with: Only our secure servers
- Privacy: Data is encrypted end-to-end
- Can be: Disabled in Settings

**SCHEDULE_EXACT_ALARM** (Android 12+)
- Used for: Scheduled tasks and cleanup
- Shared with: System WorkManager
- Why needed: Battery-efficient background work

---

## 6. Third-Party Services

### 6.1 Libraries & Dependencies

| Library | Purpose | Data Shared |
|---------|---------|-------------|
| Dagger Hilt | Dependency injection | None |
| Jetpack Compose | UI framework | None |
| Room | Local database | None |
| DataStore | Secure preferences | None |
| WorkManager | Background tasks | None |
| TensorFlow Lite | AI inference | None |
| Firebase (optional) | Cloud sync | Encrypted only |

### 6.2 Google Play Services

Android devices may use:
- **Google Play Analytics** (can be disabled)
- **Firebase Crashlytics** (anonymized crashes, opt-in)

---

## 7. Children's Privacy

SmartCallShield is not intended for children under 13.

If parents/guardians use SmartCallShield on children's devices:
- All data remains on the device
- No tracking of children
- No behavioral profiling
- Adult controls recommended

---

## 8. Data Subject Rights (GDPR/CCPA)

### 8.1 Your Rights

You have the right to:

✅ **Access**: View all your data in app settings  
✅ **Portability**: Export your call logs and contacts  
✅ **Deletion**: Delete any or all data permanently  
✅ **Control**: Disable analytics, cloud sync anytime  
✅ **Opt-out**: Disable all optional features  

### 8.2 How to Exercise Rights

1. **Access Your Data**: Settings → Data Management → View All
2. **Export Data**: Settings → Data Management → Export as CSV
3. **Delete Data**: Settings → Data Management → Clear All
4. **Disable Analytics**: Settings → Privacy → Analytics (toggle off)
5. **Disable Cloud Sync**: Settings → Cloud → Sync (toggle off)

### 8.3 Requests to Company

For legal requests (GDPR, CCPA):
- Email: privacy@smartcallshield.com
- Response time: 30 days
- Authentication required for security

---

## 9. Data Sharing

### 9.1 We Share Your Data With

**Nobody.** Your data is not shared with:
- ❌ Marketing companies
- ❌ Data brokers
- ❌ Advertisers
- ❌ Other apps
- ❌ Social media platforms
- ❌ Government (except legal compulsion)

### 9.2 Legal Compulsion

We may disclose data if required by law:
- Subpoena from court
- GDPR lawful basis
- National security request
- **We will notify you if legally permitted**

---

## 10. Security Measures

### 10.1 Technical Safeguards

```
┌─────────────────────────────────────┐
│  User Input                         │
└────────────────┬────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│  Input Validation & Sanitization    │
└────────────────┬────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│  Encrypted Local Storage (AES-256)  │
└────────────────┬────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│  Access Control (App-level)         │
└────────────────┬────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│  TLS 1.3 for Cloud Communication    │
└─────────────────────────────────────┘
```

### 10.2 Best Practices

- Device lock screen enabled
- Automatic session timeout (30 min)
- No hardcoded credentials
- Regular security audits
- Vulnerability disclosure program

### 10.3 Data Breach Response

In the unlikely event of a breach:
1. Immediate investigation
2. Notification within 72 hours (GDPR)
3. Mitigation steps communicated
4. Free credit monitoring offered (if applicable)

---

## 11. Updates to This Policy

We may update this policy when:
- Laws change
- We add new features
- Security practices evolve

**Changes effective upon in-app notification and user acceptance.**

---

## 12. Contact Us

**Privacy Questions?**
- Email: privacy@smartcallshield.com
- Mail: SmartCallShield Privacy, [Address]
- Support: https://support.smartcallshield.com

**Data Protection Officer (GDPR):**
- Email: dpo@smartcallshield.com
- Response time: 48 hours

**For California Residents (CCPA):**
- Your rights under CCPA are fully respected
- Requests processed within 45 days
- No discrimination for exercising rights

---

## 13. Jurisdiction

This privacy policy is governed by:
- **Primary**: US Law (California Consumer Privacy Act)
- **EU Users**: GDPR compliance required
- **Other**: Applicable local laws

---

## 14. Policy Summary

| Aspect | Status |
|--------|--------|
| On-device processing | ✅ Yes |
| End-to-end encryption | ✅ Yes (cloud) |
| Data minimization | ✅ Yes |
| Purpose limitation | ✅ Yes |
| User control | ✅ Full |
| Transparency | ✅ Complete |
| Data sharing | ✅ None |
| Ads or tracking | ✅ None |

---

**SmartCallShield Privacy: Your data, your control, your peace of mind.**

*Last updated: January 2025*  
*Version: 1.0*

