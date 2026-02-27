# Contributing to SmartCallShield

Thank you for your interest in contributing to SmartCallShield! We welcome contributions from developers of all skill levels.

## 🤝 Code of Conduct

We are committed to providing a welcoming and inclusive environment for all contributors. Please be respectful and constructive in all interactions.

## 📋 How to Contribute

### 1. Report Bugs

Found a bug? Please report it on GitHub Issues with:
- Clear description of the issue
- Steps to reproduce
- Expected vs actual behavior
- Device and Android version
- Screenshots/logs if applicable

### 2. Suggest Features

Have an idea? Open a GitHub Discussion or Issue with:
- Clear feature description
- Why this feature would be useful
- Potential implementation approach
- Any alternative solutions considered

### 3. Submit Code Changes

#### Setup Development Environment

```bash
# Clone repository
git clone https://github.com/akshaglobal/SmartCallShield.git
cd SmartCallShield

# Create feature branch
git checkout -b feature/your-feature-name

# Setup local development
./gradlew build
```

#### Code Style & Standards

We follow these conventions:

**Kotlin Style Guide**
- Use official Kotlin conventions
- Lines max 120 characters
- 4-space indentation
- Named parameters for complex functions

```kotlin
// ✅ Good
val isValid = user.age > 18 && user.hasPermission
user.sendNotification(
    title = "Hello",
    message = "Welcome",
    priority = NotificationPriority.HIGH
)

// ❌ Bad
val isValid = user.age > 18 && user.hasPermission;
user.sendNotification("Hello", "Welcome", NotificationPriority.HIGH)
```

**Naming Conventions**
- Classes: PascalCase (e.g., `UserRepository`)
- Functions/variables: camelCase (e.g., `getUserById`)
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_RETRIES`)
- Private members: underscore prefix (e.g., `_apiService`)

**Architecture Guidelines**
- Data layer: Room entities, DAOs, repositories
- Domain layer: Use cases, interfaces
- Presentation: ViewModels, Compose screens
- Services: BroadcastReceivers, background tasks

#### Making Changes

1. **Create a feature branch**
```bash
git checkout -b feature/add-call-recording
```

2. **Make your changes**
   - Follow code style guidelines
   - Add/update tests
   - Update documentation
   - Keep commits focused and atomic

3. **Test locally**
```bash
# Run unit tests
./gradlew test

# Run on device/emulator
./gradlew installDebug
```

4. **Commit with clear messages**
```bash
# Good commit messages
git commit -m "Add call recording feature to premium users"
git commit -m "Fix spam detection accuracy to 90%"
git commit -m "Update privacy policy for GDPR compliance"

# Bad commit messages
git commit -m "fix stuff"
git commit -m "WIP"
```

5. **Push and create PR**
```bash
git push origin feature/add-call-recording
```

### 4. Pull Request Process

1. **Before submitting**
   - [ ] Code follows style guidelines
   - [ ] All tests pass locally
   - [ ] Commit messages are clear
   - [ ] No unrelated changes included
   - [ ] Documentation updated

2. **PR Description Template**
```markdown
## Description
Brief description of changes

## Related Issues
Closes #123

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Performance improvement

## Testing
How to test these changes:
1. Step 1
2. Step 2

## Screenshots (if applicable)
...

## Checklist
- [ ] Tests pass
- [ ] Code follows style guide
- [ ] Comments added
- [ ] Documentation updated
```

3. **Review Process**
   - At least one approval required
   - All tests must pass
   - No merge conflicts
   - Code review suggestions addressed

4. **Merge**
   - Squash and merge for single commits
   - Keep history clean

## 📚 Architecture & Code Organization

### Directory Structure
```
app/src/main/java/com/akshaglobal/smartcallshield/
├── data/                    # Data layer
│   ├── dao/               # Room DAOs
│   ├── database/          # Room Database
│   ├── model/             # Entities
│   ├── preferences/       # DataStore
│   └── repository/        # Repositories
├── domain/                  # Domain layer
│   └── usecase/           # Use cases
├── presentation/            # Presentation layer
│   └── ui/
│       ├── screens/       # Compose screens
│       ├── viewmodel/     # ViewModels
│       ├── navigation/    # Navigation
│       └── theme/         # UI theme
├── service/                 # Services
│   ├── ai/               # AI models
│   └── *.kt              # BroadcastReceivers
├── di/                      # Dependency Injection
└── SmartCallShieldApp.kt   # App class
```

### Adding a New Feature

Example: Add block list sync feature

1. **Data Layer**
```kotlin
// Create entity
@Entity(tableName = "sync_logs")
data class SyncLogEntity(...)

// Create DAO
@Dao
interface SyncLogDao { ... }

// Add to database
@Database(entities = [..., SyncLogEntity::class])
abstract class SmartCallShieldDatabase { ... }

// Create repository
@Singleton
class SyncRepository @Inject constructor(...) { ... }
```

2. **Domain Layer**
```kotlin
// Create use case
class SyncBlockListUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(): Result<SyncStatus> { ... }
}
```

3. **Presentation Layer**
```kotlin
// Create ViewModel
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncUseCase: SyncBlockListUseCase
) : ViewModel() { ... }

// Create or update Screen
@Composable
fun SyncScreen(viewModel: SyncViewModel = hiltViewModel()) { ... }
```

4. **Update Navigation**
```kotlin
// Add to Screen sealed class
object Sync : Screen("sync", "Sync")

// Add to navigation
NavHost(...) {
    composable(Screen.Sync.route) {
        SyncScreen()
    }
}
```

## 🧪 Testing

### Unit Tests
```kotlin
// Example: Repository test
@RunWith(RobolectricTestRunner::class)
class ContactRepositoryTest {
    @Test
    fun testAddContact() {
        // Arrange
        val contact = ContactEntity(phoneNumber = "+1234567890")
        
        // Act
        repository.addContact(contact)
        
        // Assert
        val result = repository.getContactByPhoneNumber("+1234567890")
        assertEquals(contact, result.first())
    }
}
```

### Instrumented Tests
```kotlin
// Example: UI test
@RunWith(AndroidJUnit4::class)
class ContactsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testAddContactFlow() {
        composeTestRule.setContent {
            ContactsScreen()
        }
        
        composeTestRule.onNodeWithText("Add Contact").performClick()
        composeTestRule.onNodeWithTag("phone_input").performTextInput("+1234567890")
        // ... more assertions
    }
}
```

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# With coverage
./gradlew test jacocoTestReport

# Specific test
./gradlew test --tests "com.akshaglobal.smartcallshield.data.ContactRepositoryTest"
```

## 📖 Documentation

### Code Comments
```kotlin
// ✅ Good: Explains WHY, not WHAT
// User needs to be authenticated before accessing call logs
// to prevent unauthorized data access
if (!user.isAuthenticated) {
    return null
}

// ❌ Bad: Obvious from code
// Check if user is authenticated
if (!user.isAuthenticated) {
    return null
}
```

### KDoc Comments
```kotlin
/**
 * Detects if a phone number is spam using AI model.
 *
 * This function extracts features from the phone number,
 * runs inference through a TensorFlow Lite model, and
 * returns a spam risk score.
 *
 * @param phoneNumber The phone number to analyze
 * @return [SpamDetectionResult] with confidence score
 * @throws Exception if model fails to initialize
 *
 * Example:
 * ```kotlin
 * val result = detectSpam("+1234567890")
 * if (result.isSpam) {
 *     blockCall(result.phoneNumber)
 * }
 * ```
 */
suspend fun detectSpam(phoneNumber: String): SpamDetectionResult
```

### Documentation Files
- Update README.md for new features
- Update PRIVACY.md if data handling changes
- Add inline comments for complex logic
- Keep documentation synchronized with code

## 🔍 Code Review Checklist

When reviewing PRs, check:

- [ ] Code follows style guidelines
- [ ] Tests included and passing
- [ ] Documentation updated
- [ ] No hardcoded values
- [ ] No unused imports/variables
- [ ] Proper error handling
- [ ] Resource cleanup (database, network)
- [ ] No security vulnerabilities
- [ ] Performance impact considered
- [ ] Privacy implications reviewed

## 🚀 Performance Guidelines

### Memory
- Avoid memory leaks in listeners
- Use WeakReference for context-heavy objects
- Clean up resources in onDestroy

### Network
- Implement proper caching
- Use pagination for large datasets
- Retry with exponential backoff

### Database
- Use indices for frequently queried fields
- Use pagination for large tables
- Avoid N+1 queries

### Example: Optimization
```kotlin
// ❌ Inefficient: Creates new Flow each time
fun getAllCalls() = database.getAll()

// ✅ Efficient: Caches and reuses
private val _callsCache = MutableStateFlow<List<CallLogEntity>>(emptyList())
val allCalls = _callsCache.asStateFlow()

fun refreshCalls() {
    viewModelScope.launch {
        _callsCache.value = database.getAll()
    }
}
```

## 🔒 Security Guidelines

- Never log sensitive data (phone numbers, messages)
- Use Android KeyStore for encryption keys
- Validate all user inputs
- Use HTTPS for API calls
- Never hardcode API keys
- Review permission usage
- Consider privacy implications

## 📝 Commit Message Guide

```
<type>(<scope>): <subject>

<body>

<footer>

# Type: feat, fix, docs, style, refactor, test, chore
# Scope: data, domain, presentation, service, di
# Subject: imperative, lowercase, no period, max 50 chars
# Body: explain what and why, wrap at 72 chars
# Footer: reference issues, breaking changes
```

Example:
```
feat(service): add ring counter logic for auto-reply

Implement ring counting mechanism to trigger SMS auto-reply
after configurable number of rings (default 5 rings).

Uses CallState tracking to monitor RINGING state transitions
and calculates elapsed time since first ring.

Closes #45
```

## 🎯 Priority Issues

Help us prioritize! Priority levels:
- **P0**: Critical bugs, security issues
- **P1**: Major features, frequent crashes
- **P2**: Nice-to-have features, minor bugs
- **P3**: Documentation, code cleanup

## 💬 Questions?

- Open a GitHub Discussion
- Email: dev@smartcallshield.com
- Create an issue for clarification

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for helping make SmartCallShield better! 🙏

