package com.akshaglobal.smartcallshield.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionDisclosureScreen(onGetStarted: () -> Unit) {
    var acceptedTerms by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Important Information",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            DisclosureCard(
                title = "Core Function",
                description = "CallSecure is designed to protect you from unwanted calls. It manages your calls automatically based on your preferred mode.",
                icon = Icons.Default.Info
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            DisclosureCard(
                title = "Call Log Access",
                description = "Required to identify incoming calls and apply your safety filters (like allowing only family).",
                icon = Icons.Default.Phone
            )
            
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Terms and Conditions Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { checked -> 
                        if (checked) {
                            showTermsDialog = true
                        } else {
                            acceptedTerms = false
                        }
                    }
                )
                Text(
                    text = "I have read and agree to the ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Terms & Conditions",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showTermsDialog = true }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onGetStarted,
                enabled = acceptedTerms,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your privacy is our priority. All data is processed locally and never leaves your device.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms & Conditions", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
            containerColor = MaterialTheme.colorScheme.surface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Last Updated: Sept 15, 2026" +
                            "\n\nWelcome to CallSecure. By downloading, installing, or using this application, you agree to these Terms & Conditions. If you do not agree with these terms, please do not use the application." +
                            "\n\n1. Acceptance of Terms\nBy using CallSecure, you agree to comply with these Terms & Conditions and all applicable laws and regulations." +
                            "\n\n2. Purpose of the Application\nCallSecure is designed to help users identify and manage incoming phone calls by providing features such as:\n\t•\tSpam and unwanted call detection\n\t•\tAI-assisted call screening\n\t•\tCaller identification\n\t•\tAutomatic call handling (where supported)\n\t•\tCall history insights and analytics\n\t•\tUser-customizable blocking and notification settings\nCallSecure is intended to assist users but does not guarantee that every spam or fraudulent call will be detected or blocked." +
                            "\n\n3. Permissions\nTo provide its features, CallSecure may request access to:\n\t•\tPhone\n\t•\tCall Logs\n\t•\tContacts\n\t•\tNotifications\nThese permissions are used only for the features you choose to enable." +
                            "\n\n4. User Responsibility\nYou agree that you are responsible for:\n\t•\tUsing the application lawfully.\n\t•\tVerifying important caller information.\n\t•\tReviewing blocked calls periodically.\n\t•\tConfiguring the application according to your needs.\nYou should not rely solely on CallSecure to determine whether a call is safe or legitimate." +
                            "\n\n5. AI Predictions\nCallSecure uses artificial intelligence and machine learning to classify incoming calls.\nPredictions are estimates based on available information and may occasionally be incorrect. Some legitimate calls may be flagged as spam, and some spam calls may not be detected." +
                            "\n\n6. Data Collection\nCallSecure may process limited information necessary to provide its services, including:\n\t•\tIncoming phone numbers\nYour contacts, conversations, and personal information are not collected.\nPlease review our Privacy Policy for complete details about data collection and processing." +
                            "\n\n7. User Privacy\nWe are committed to protecting your privacy.\nPersonal information is processed only as necessary to provide the requested features and improve the application." +
                            "\n\n8. Internet Connectivity\nSome features require an active internet connection, including:\n\t•\tCloud-based services\n\t•\tDatabase Synchronization\nOffline functionality may be limited." +
                            "\n\n9. Updates\nWe may update the application to:\n\t•\tImprove spam detection\n\t•\tAdd new features\n\t•\tFix bugs\n\t•\tImprove security\n\t•\tEnhance AI models\nSome features may change or be discontinued without prior notice." +
                            "\n\n10. Limitation of Liability\nCallSecure is provided \"as is\" without warranties of any kind.\nWe are not responsible for:\n\t•\tMissed calls\n\t•\tIncorrect spam classifications\n\t•\tLoss of business\n\t•\tFinancial losses\n\t•\tDevice incompatibility\n\t•\tService interruptions\n\t•\tNetwork failures\nUse the application at your own discretion." +
                            "\n\n11. Prohibited Use\nYou agree not to:\n\t•\tReverse engineer the application.\n\t•\tUse the application for illegal purposes.\n\t•\tAttempt to bypass security features.\n\t•\tDistribute modified versions without Authorization.\n\t•\tMisuse the service to harass or harm others.\n\n12. Intellectual Property\nAll application content, including software, logos, designs, icons, graphics, and trademarks, is owned by CallSecure or its licensors and is protected by applicable intellectual property laws." +
                            "\n\n13. Account and Settings\nIf the application offers cloud Synchronization or account-based features, you are responsible for maintaining the security of your account and device." +
                            "\n\n14. Termination\nWe reserve the right to suspend or terminate access to the application if these Terms & Conditions are violated or if misuse of the application is detected and will be prosecuted ." +
                            "\n\n15. Changes to These Terms\nThese Terms & Conditions may be updated periodically.\nThe latest version will always be available within the application. Continued use of CallSecure after changes become effective constitutes acceptance of the updated terms." +
                            "\n\n16. Contact Us\nIf you have questions, feedback, or concerns regarding these Terms & Conditions, please contact us using the support option provided within the application or through our official website."+
                            "\n\n17  CallSecure is not intended for children under the age of 13 (or the minimum age required by applicable law of the country app installed by the user).\n"+
                            "\n\n18. Acceptance\nBy installing or using CallSecure, you acknowledge that you have read, understood, and agree to these Terms & Conditions.", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            confirmButton = {
                Button(onClick = {
                    acceptedTerms = true
                    showTermsDialog = false
                }) {
                    Text("Agree")
                }
            },
            dismissButton = {
                Button(onClick = { 
                    acceptedTerms = false
                    showTermsDialog = false 
                }) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.fillMaxWidth(0.95f).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge)
        )
    }
}

@Composable
private fun DisclosureCard(title: String, description: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
