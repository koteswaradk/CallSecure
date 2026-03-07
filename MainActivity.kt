// ...existing code...
import com.akshaglobal.smartcallshield.presentation.ui.screens.IntroScreen
import com.akshaglobal.smartcallshield.presentation.ui.screens.SplashScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// ...existing code...
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartCallShieldTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val context = this@MainActivity
                    var showIntro by remember { mutableStateOf(false) }
                    var showSplash by remember { mutableStateOf(false) }
                    var showMain by remember { mutableStateOf(false) }

                    val prefs = context.getSharedPreferences("smartcallshield_prefs", Context.MODE_PRIVATE)
                    showIntro = !prefs.getBoolean("intro_shown", false)

                    when {
                        showIntro -> IntroScreen(navController = null, context = context) {
                            showIntro = false
                            showSplash = true
                        }
                        showSplash -> SplashScreen(context = context) {
                            showSplash = false
                            showMain = true
                        }
                        else -> MainNavigation()
                    }
                }
            }
        }
    }
// ...existing code...

