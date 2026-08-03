package com.akshaglobal.smartcallshield.presentation.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.border
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.akshaglobal.smartcallshield.common_ui.components.SettingCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.akshaglobal.smartcallshield.R
import com.akshaglobal.smartcallshield.data.model.CallMode
import com.akshaglobal.smartcallshield.util.ReviewUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.akshaglobal.smartcallshield.presentation.viewmodel.SettingsViewModel
import com.akshaglobal.smartcallshield.presentation.viewmodel.CallModesViewModel
import com.google.accompanist.permissions.isGranted
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.graphics.pdf.PdfDocument
import androidx.compose.material3.CircularProgressIndicator
import java.io.File
import java.io.FileOutputStream
import com.akshaglobal.smartcallshield.presentation.ui.screens.SettingsSectionHeader
import com.akshaglobal.smartcallshield.presentation.ui.screens.CallModeManagementCard
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: SettingsViewModel = hiltViewModel(),
    isAppEnabled: Boolean?, // Accept nullable for loading state
    callModesViewModel: CallModesViewModel = hiltViewModel()
) {
    val spamDetectionEnabled by viewModel.spamDetectionEnabled.collectAsState()
    val drivingModeEnabled by viewModel.drivingModeEnabled.collectAsState()
    val drivingModeAutoReply by viewModel.drivingModeAutoReply.collectAsState()
    val drivingModeAutoReplyEnabled by viewModel.drivingModeAutoReplyEnabled.collectAsState()
    val spamConfidenceThreshold by viewModel.spamConfidenceThreshold.collectAsState()
    val autoRejectSpam by viewModel.autoRejectSpam.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val context = LocalContext.current
    val enabledModesState = callModesViewModel.enabledModes.collectAsState()
    val enabledModes = enabledModesState.value
    var showHowToUseDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsAndConditionsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        callModesViewModel.loadModes()
    }

    val isDrivingModeValid = enabledModes["DRIVING"] == true

    if (isAppEnabled == null) {
        // Show loading indicator while state is loading
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp)
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp) // Professional constraint for tablets
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Text(
                "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 16.dp)
            )

        // --- PRIMARY FEATURE: DRIVING MODE ---
        SettingsSectionHeader("Driving Mode & Auto-Reply")
        SettingCard(
            title = "Enable Driving Mode",
            description = "Automatically handle calls while driving",
            isEnabled = drivingModeEnabled,
            onToggle = { viewModel.setDrivingModeEnabled(it) },
            enabled = isAppEnabled && isDrivingModeValid && currentMode == CallMode.DRIVING
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingCard(
            title = "Enable Auto-Reply SMS",
            description = "Send SMS automatically to allowed contacts",
            isEnabled = drivingModeAutoReplyEnabled,
            onToggle = { viewModel.setDrivingModeAutoReplyEnabled(it) },
            enabled = isAppEnabled && currentMode == CallMode.DRIVING && drivingModeEnabled
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Auto-Reply Message",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        OutlinedTextField(
            value = drivingModeAutoReply,
            onValueChange = { viewModel.setDrivingModeAutoReply(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(8.dp),
            label = { Text("Reply message") },
            maxLines = 5,
            enabled = isAppEnabled && !drivingModeAutoReplyEnabled,
            placeholder = { Text("Enter auto-reply message") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        )
        if (drivingModeAutoReplyEnabled) {
            Text(
                "Disable Auto-Reply to edit the message.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- MODES MANAGEMENT ---
        SettingsSectionHeader("Call Modes Management")
        CallModeManagementCard()

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECONDARY FEATURE: SPAM & PROTECTION ---
        SettingsSectionHeader("Spam & Call Protection")
        SettingCard(
            title = "Enable Spam Detection",
            description = "Use AI to detect spam calls",
            isEnabled = spamDetectionEnabled,
            onToggle = { viewModel.setSpamDetectionEnabled(it) },
            enabled = isAppEnabled
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingCard(
            title = "Auto-Reject Spam",
            description = "Automatically reject detected spam calls",
            isEnabled = autoRejectSpam,
            onToggle = { viewModel.setAutoRejectSpam(it) },
            enabled = isAppEnabled
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Confidence Threshold: ${String.format(Locale.getDefault(), "%.1f", spamConfidenceThreshold * 100)}%",
            modifier = Modifier.padding(8.dp)
        )
        Slider(
            value = spamConfidenceThreshold,
            onValueChange = { viewModel.setSpamConfidenceThreshold(it) },
            valueRange = 0.5f..1.0f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy Section
        SettingsSectionHeader("Privacy & Data")
        SettingCard(
            title = "Analytics",
            description = "Help improve the app with usage data",
            isEnabled = true,
            onToggle = { }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Legal Sections
        SettingsSectionHeader("Legal")
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
            LegalItemCard(
                title = "Privacy Policy",
                description = "How we handle your data",
                onClick = { showPrivacyPolicyDialog = true }
            )
            Spacer(modifier = Modifier.height(8.dp))
            LegalItemCard(
                title = "Terms and Conditions",
                description = "General terms for DriveShield",
                onClick = { showTermsAndConditionsDialog = true }
            )
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                LegalItemCard(
                    title = "Privacy Policy",
                    description = "How we handle your data",
                    onClick = { showPrivacyPolicyDialog = true },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                LegalItemCard(
                    title = "Terms & Conditions",
                    description = "General terms",
                    onClick = { showTermsAndConditionsDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About Section
        SettingsSectionHeader("About App")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = R.string.about_version, "1.0"),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.app_description),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showHowToUseDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("How to Use & Features")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        ReviewUtils.openPlayStoreListing(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Rate & Review App")
                }
            }
        }

        if (showHowToUseDialog) {
            AlertDialog(
                onDismissRequest = { showHowToUseDialog = false },
                title = { Text("How to Use DriveShield", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.surface,
                textContentColor = MaterialTheme.colorScheme.onSurface,
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("🎯 Core Features:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("• AI Spam Detection: Automatically identifies and blocks potential spam calls.", color = MaterialTheme.colorScheme.onSurface)
                        Text("• Smart Call Modes: Switch between Normal, Family, Driving, and Emergency modes.", color = MaterialTheme.colorScheme.onSurface)
                        Text("• Contact Management: Whitelist your trusted contacts for each mode.", color = MaterialTheme.colorScheme.onSurface)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("🚗 Driving Mode:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("• Once enabled, callers in your Driving contact list will hear the phone ring for 5 seconds.", color = MaterialTheme.colorScheme.onSurface)
                        Text("• If not answered, the call is rejected and an auto-reply SMS is sent.", color = MaterialTheme.colorScheme.onSurface)
                        Text("• All other calls are rejected immediately for your safety.", color = MaterialTheme.colorScheme.onSurface)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("🛡️ Setup Guide:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("1. Enable the app using the switch on the Dashboard.", color = MaterialTheme.colorScheme.onSurface)
                        Text("2. Grant necessary permissions (Phone, SMS, Contacts).", color = MaterialTheme.colorScheme.onSurface)
                        Text("3. Go to 'Call Modes' to add contacts to Family, Driving, or Emergency lists.", color = MaterialTheme.colorScheme.onSurface)
                        Text("4. Set DriveShield as your default Caller ID & Spam app for best results.", color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                confirmButton = {
                    Button(onClick = { showHowToUseDialog = false }) {
                        Text("Got it!")
                    }
                },
                modifier = Modifier.fillMaxWidth(0.95f).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge)
            )
        }

        if (showPrivacyPolicyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyPolicyDialog = false },
                title = { Text("Privacy Policy", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.surface,
                textContentColor = MaterialTheme.colorScheme.onSurface,
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("SMS and Call Log data are processed locally on the device and never uploaded to a server\n\nNot collecting any personal data from the user\n\nNot handling any kind of data manipulation of the calls\n\nOnce the app is deleted from the device complete data will be deleted", color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                confirmButton = {
                    Button(onClick = { showPrivacyPolicyDialog = false }) {
                        Text("Close")
                    }
                },
                modifier = Modifier.fillMaxWidth(0.95f).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge)
            )
        }

        if (showTermsAndConditionsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsAndConditionsDialog = false },
                title = { Text("Terms & Conditions", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.surface,
                textContentColor = MaterialTheme.colorScheme.onSurface,
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Last Updated: June 30, 2026" +
                                "\n\nWelcome to DriveShield. By downloading, installing, or using this application, you agree to these Terms & Conditions. If you do not agree with these terms, please do not use the application." +
                                "\n\n1. Acceptance of Terms\nBy using DriveShield, you agree to comply with these Terms & Conditions and all applicable laws and regulations." +
                                "\n\n2. Purpose of the Application\nDriveShield is designed to help users identify and manage incoming phone calls by providing features such as:\n\t•\tSpam and unwanted call detection\n\t•\tAI-assisted call screening\n\t•\tCaller identification\n\t•\tAutomatic call handling (where supported)\n\t•\tAutomatic SMS replies while driving (optional)\n\t•\tDriving mode detection\n\t•\tCall history insights and analytics\n\t•\tUser-customizable blocking and notification settings\nDriveShield is intended to assist users but does not guarantee that every spam or fraudulent call will be detected or blocked." +
                                "\n\n3. Permissions\nTo provide its features, DriveShield may request access to:\n\t•\tPhone\n\t•\tCall Logs\n\t•\tContacts\n\t•\tNotifications\n\t•\tSMS (only if you enable automatic SMS replies)\n\t•\tAccessibility services (if required for optional features)\nThese permissions are used only for the features you choose to enable." +
                                "\n\n4. User Responsibility\nYou agree that you are responsible for:\n\t•\tUsing the application lawfully.\n\t•\tVerifying important caller information.\n\t•\tReviewing blocked calls periodically.\n\t•\tConfiguring the application according to your needs.\nYou should not rely solely on DriveShield to determine whether a call is safe or legitimate." +
                                "\n\n5. AI Predictions\nDriveShield uses artificial intelligence and machine learning to classify incoming calls.\nPredictions are estimates based on available information and may occasionally be incorrect. Some legitimate calls may be flagged as spam, and some spam calls may not be detected." +
                                "\n\n6. Driving Mode\nDriving Mode is an optional feature that automatically detects when you may be driving.\nUsers are responsible for enabling or disabling this feature as needed." +
                                "\n\n7. Data Collection\nDriveShield may process limited information necessary to provide its services, including:\n\t•\tIncoming phone numbers\nYour contacts, conversations, and personal information are not collected.\nPlease review our Privacy Policy for complete details about data collection and processing." +
                                "\n\n8. User Privacy\nWe are committed to protecting your privacy.\nPersonal information is processed only as necessary to provide the requested features and improve the application." +
                                "\n\n9. Internet Connectivity\nSome features require an active internet connection, including:\n\t•\tCloud-based services\n\t•\tDatabase Synchronization\nOffline functionality may be limited." +
                                "\n\n10. Updates\nWe may update the application to:\n\t•\tImprove spam detection\n\t•\tAdd new features\n\t•\tFix bugs\n\t•\tImprove security\n\t•\tEnhance AI models\nSome features may change or be discontinued without prior notice." +
                                "\n\n11. Limitation of Liability\nDriveShield is provided \"as is\" without warranties of any kind.\nWe are not responsible for:\n\t•\tMissed calls\n\t•\tIncorrect spam classifications\n\t•\tLoss of business\n\t•\tFinancial losses\n\t•\tDevice incompatibility\n\t•\tService interruptions\n\t•\tNetwork failures\nUse the application at your own discretion." +
                                "\n\n12. Prohibited Use\nYou agree not to:\n\t•\tReverse engineer the application.\n\t•\tUse the application for illegal purposes.\n\t•\tAttempt to bypass security features.\n\t•\tDistribute modified versions without Authorization.\n\t•\tMisuse the service to harass or harm others.\n\n13. Intellectual Property\nAll application content, including software, logos, designs, icons, graphics, and trademarks, is owned by DriveShield or its licensors and is protected by applicable intellectual property laws." +
                                "\n\n14. Account and Settings\nIf the application offers cloud Synchronization or account-based features, you are responsible for maintaining the security of your account and device." +
                                "\n\n15. Termination\nWe reserve the right to suspend or terminate access to the application if these Terms & Conditions are violated or if misuse of the application is detected and will be prosecuted ." +
                                "\n\n16. Changes to These Terms\nThese Terms & Conditions may be updated periodically.\nThe latest version will always be available within the application. Continued use of DriveShield after changes become effective constitutes acceptance of the updated terms." +
                                "\n\n17. Contact Us\nIf you have questions, feedback, or concerns regarding these Terms & Conditions, please contact us using the support option provided within the application or through our official website." +"10. Children's Privacy\n" +
                                "\n\n18  DriveShield is not intended for children under the age of 13 (or the minimum age required by applicable law).\n"+
                                "\n\n19. Acceptance\nBy installing or using DriveShield, you acknowledge that you have read, understood, and agree to these Terms & Conditions.", color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                confirmButton = {
                    Button(onClick = { showTermsAndConditionsDialog = false }) {
                        Text("Close")
                    }
                },
                modifier = Modifier.fillMaxWidth(0.95f).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
}


@Composable
fun LegalItemCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

