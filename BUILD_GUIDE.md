# SmartCallShield Build & Deployment Guide

## Pre-Release Checklist

### 1. Code Quality
- [ ] All compilation warnings resolved
- [ ] ProGuard rules applied and tested
- [ ] No hardcoded secrets or credentials
- [ ] Code formatted with ktlint
- [ ] No unused imports or variables
- [ ] All TODOs documented in GitHub issues

### 2. Testing
- [ ] Unit tests passing (>80% coverage)
  ```bash
  ./gradlew test
  ```
- [ ] Instrumentation tests passing on device/emulator
  ```bash
  ./gradlew connectedAndroidTest
  ```
- [ ] Manual QA on Android 10, 12, 14, 15
- [ ] Permission flows tested on actual devices
- [ ] Call interception tested (requires device)

### 3. Performance
- [ ] APK size < 10 MB
- [ ] Initial startup < 2 seconds
- [ ] Memory usage < 100 MB baseline
- [ ] Battery impact measured (< 2% per hour)
- [ ] Database queries optimized (< 50ms each)

### 4. Security
- [ ] Permissions reviewed by security team
- [ ] Encryption keys properly managed
- [ ] No sensitive data in logs
- [ ] ProGuard rules verified
- [ ] Dependencies scanned for CVEs
- [ ] Privacy policy reviewed and updated
- [ ] GDPR/CCPA compliance verified

### 5. Documentation
- [ ] README.md complete and accurate
- [ ] PRIVACY.md comprehensive
- [ ] CODE_OF_CONDUCT.md created
- [ ] CONTRIBUTING.md for developers
- [ ] API documentation (if applicable)
- [ ] Architecture diagrams updated

### 6. Release Assets
- [ ] Version bumped (major.minor.patch)
- [ ] CHANGELOG.md updated
- [ ] App signing certificate created and backed up
- [ ] Release notes prepared
- [ ] Play Store screenshots prepared
- [ ] App description updated

---

## Building for Release

### 1. Configure Release Build

Edit `app/build.gradle.kts`:
```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
    
    signingConfig = signingConfigs.release
}
```

### 2. Create Signing Key (One-time)

```bash
# Generate release key
keytool -genkey -v -keystore ~/.android/smartcallshield.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias smartcallshield

# Backup the key (IMPORTANT!)
cp ~/.android/smartcallshield.keystore ./backup/smartcallshield.keystore
```

### 3. Configure Signing in `local.properties`

```properties
# local.properties (NEVER commit to git)
RELEASE_STORE_FILE=/path/to/smartcallshield.keystore
RELEASE_STORE_PASSWORD=your_password
RELEASE_KEY_ALIAS=smartcallshield
RELEASE_KEY_PASSWORD=your_password
```

### 4. Build Release APK

```bash
./gradlew clean build -Prelease
```

### 5. Build Release AAB (for Play Store)

```bash
./gradlew bundleRelease
```

Output location: `app/build/outputs/bundle/release/app-release.aab`

---

## Play Store Publishing

### 1. Set Up Play Console Account
- [ ] Developer account created
- [ ] Payment method added
- [ ] Developer agreement signed

### 2. Create App in Play Console
1. Go to Play Console → Create New App
2. Select "SmartCallShield"
3. Accept agreement
4. Choose default language (English)

### 3. Fill Store Listing
- **App Name**: SmartCallShield
- **Short Description** (50 chars):
  > AI-powered spam call blocker with privacy-first design

- **Full Description** (4000 chars):
  ```
  SmartCallShield is a privacy-focused call management app that uses 
  AI to detect and block spam calls while respecting your privacy.
  
  Features:
  • AI-powered spam detection (on-device)
  • Whitelist/Blacklist call filtering
  • Driving mode with auto-reply SMS
  • Multiple call modes (Normal, Family, Emergency)
  • Zero data collection without consent
  • 100% privacy-focused design
  
  Privacy First:
  - All processing happens on your device
  - No personal data is sent to our servers
  - Completely offline-capable
  - Full GDPR & CCPA compliance
  ```

### 4. Add Screenshots
- 5-8 screenshots (540x720 or 1080x1440 px)
- Highlight key features
- Include captions

### 5. Add Privacy Policy
- Link: https://github.com/akshaglobal/SmartCallShield/blob/main/PRIVACY.md
- Ensure GDPR/CCPA compliant

### 6. Set Content Rating
1. Go to Content Rating → Questionnaire
2. Answer all questions
3. Get rating certificate

### 7. Set Audience
- Target audience: Teens and up
- Contains: No ads, no in-app purchases (free version)

### 8. Set Permissions
1. Review permission disclosure
2. Ensure all permissions justified:
   - Phone: Call blocking
   - SMS: Auto-reply
   - Contacts: Whitelist matching
   - Location: Optional driving detection

### 9. Prepare Release
1. Set version code (1, 2, 3...)
2. Set version name (1.0, 1.1, 1.2...)
3. Upload AAB file
4. Review app bundle analysis

### 10. Test Release
1. Use internal testing track first
2. Add test users to Google Group
3. Send test link via Google Play
4. Test on various devices for 48+ hours

### 11. Staged Rollout
1. Start with 5% rollout
2. Monitor crash rate and ratings
3. Increase to 25%, 50%, 100% as confident

### 12. Monitor Post-Launch
- [ ] Check crash reports daily
- [ ] Respond to reviews
- [ ] Monitor ratings (target: 4.5+)
- [ ] Track downloads and DAU
- [ ] Monitor permission acceptance rate

---

## Version Numbering

**Format**: `MAJOR.MINOR.PATCH`

- **MAJOR**: Significant features, major UI overhaul
- **MINOR**: New features, improvements
- **PATCH**: Bug fixes, minor updates

Example:
- v1.0 - Initial release
- v1.1 - Premium features
- v1.2 - Bug fixes
- v2.0 - Major redesign

---

## Release Notes Template

```markdown
# SmartCallShield v1.0.0

## What's New
- Initial release
- AI-powered spam detection
- Multiple call modes
- Analytics dashboard

## Improvements
- Optimized database queries
- Improved UI responsiveness
- Better contact matching

## Bug Fixes
- Fixed permission handling on Android 12+
- Fixed driving mode crash
- Improved notification reliability

## Known Issues
- Call rejection may not work on some devices
- GPS detection consumes more battery

## Thank You
Thank you to our beta testers!
```

---

## Monitoring & Maintenance

### Daily Tasks
- Check crash reports and error logs
- Review user ratings and comments
- Monitor app performance metrics

### Weekly Tasks
- Analyze usage statistics
- Review privacy compliance
- Check for security updates in dependencies

### Monthly Tasks
- Plan next release
- Prioritize bug fixes and features
- Update documentation

---

## Continuous Deployment (Optional)

For automated releases with GitHub Actions:

```yaml
name: Release to Play Store
on:
  push:
    tags:
      - 'v*'
jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build AAB
        run: ./gradlew bundleRelease
      - name: Upload to Play Store
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.PLAY_STORE_KEY }}
          packageName: com.akshaglobal.smartcallshield
          releaseFiles: 'app/build/outputs/bundle/release/app-release.aab'
          track: production
```

---

## Emergency Rollback

If critical issues found post-launch:

1. **Identify Issue**: Check crash logs and user reports
2. **Create Hotfix**: Branch from release tag
3. **Test Thoroughly**: On multiple devices
4. **Increment Version**: v1.0.1 for patch
5. **Staged Rollout**: Start with 10% instead of 100%
6. **Post Mortem**: Document what went wrong

---

## Post-Launch Support

### First 48 Hours
- Monitor crash reports hourly
- Respond to user questions
- Watch for permission issues

### First Week
- Address critical bugs with v1.0.1
- Collect feature requests
- Improve documentation based on feedback

### Ongoing
- Regular security updates
- Feature releases every 4-6 weeks
- Community engagement
- Documentation updates

---

**Ready to launch SmartCallShield to the world! 🚀**

