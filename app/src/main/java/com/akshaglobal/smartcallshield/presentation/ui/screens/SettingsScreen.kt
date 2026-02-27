package com.akshaglobal.smartcallshield.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.akshaglobal.smartcallshield.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val spamDetectionEnabled by viewModel.spamDetectionEnabled.collectAsState()
    val drivingModeEnabled by viewModel.drivingModeEnabled.collectAsState()
    val drivingModeAutoReply by viewModel.drivingModeAutoReply.collectAsState()
    val spamConfidenceThreshold by viewModel.spamConfidenceThreshold.collectAsState()
    val autoRejectSpam by viewModel.autoRejectSpam.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Spam Detection Section
        SettingsSectionHeader("Spam Detection")
        SettingCard(
            title = "Enable Spam Detection",
            description = "Use AI to detect spam calls",
            isEnabled = spamDetectionEnabled,
            onToggle = { viewModel.setSpamDetectionEnabled(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingCard(
            title = "Auto-Reject Spam",
            description = "Automatically reject detected spam calls",
            isEnabled = autoRejectSpam,
            onToggle = { viewModel.setAutoRejectSpam(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Confidence Threshold: ${String.format("%.1f", spamConfidenceThreshold * 100)}%",
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

        // Driving Mode Section
        SettingsSectionHeader("Driving Mode")
        SettingCard(
            title = "Enable Driving Mode",
            description = "Automatically handle calls while driving",
            isEnabled = drivingModeEnabled,
            onToggle = { viewModel.setDrivingModeEnabled(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Auto-Reply Message",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        TextField(
            value = drivingModeAutoReply,
            onValueChange = { viewModel.setDrivingModeAutoReply(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp),
            label = { Text("Reply message") },
            maxLines = 5
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

        SettingCard(
            title = "Cloud Sync (Premium)",
            description = "Sync your settings across devices",
            isEnabled = false,
            onToggle = { }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(vertical = 12.dp)
            .padding(start = 8.dp)
    )
}

@Composable
private fun SettingCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(description, fontSize = 12.sp, color = Color.Gray)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

