package com.akshaglobal.smartcallshield.presentation.ui.screens

import android.util.Log
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.border
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshaglobal.smartcallshield.R
import com.akshaglobal.smartcallshield.data.model.CallMode
import com.akshaglobal.smartcallshield.presentation.viewmodel.CallModesViewModel
import com.akshaglobal.smartcallshield.presentation.viewmodel.DashboardViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.akshaglobal.smartcallshield.CallSecureApp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.core.net.toUri

import androidx.compose.foundation.layout.size
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import com.akshaglobal.smartcallshield.presentation.ui.components.ActivityStatCard
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ViewGroup
import android.content.Context
import androidx.compose.ui.graphics.toArgb
import com.akshaglobal.smartcallshield.presentation.ui.components.ModeButton

@Composable
fun DashboardScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: DashboardViewModel = hiltViewModel(),
    callModesViewModel: CallModesViewModel = hiltViewModel()
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val isAppEnabled by viewModel.isAppEnabled.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val modes by callModesViewModel.modes.collectAsState()
    val enabledModes by callModesViewModel.enabledModes.collectAsState()
    var showModeErrorDialog by remember { mutableStateOf<String?>(null) }
    var showEnableDialog by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(false) }
    var showDisableDialog by remember { mutableStateOf(false) }
    var showModeChangeDialog by remember { mutableStateOf(false) }
    var pendingMode: CallMode? by remember { mutableStateOf(null) }
    val deviceContacts by callModesViewModel.deviceContacts.collectAsState()
    var lastSelectedMode by remember { mutableStateOf<CallMode?>(currentMode) }

    // Sync lastSelectedMode with currentMode whenever currentMode changes
    LaunchedEffect(currentMode) {
        lastSelectedMode = currentMode
    }

    // Always reload modes and contacts from the database when DashboardScreen is recomposed
    LaunchedEffect(Unit) {
        callModesViewModel.loadModes()
    }
    LaunchedEffect(modes, deviceContacts) {
        callModesViewModel.syncModesAndContacts()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter // Center content for wide screens
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp), // Professional constraint for tablets
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                )
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "CallSecure",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        // Switch is always enabled so user can interact
                        Switch(
                            checked = isAppEnabled,
                            onCheckedChange = { checked ->
                                if (checked && !isAppEnabled) {
                                    showEnableDialog = true
                                } else if (!checked && isAppEnabled) {
                                    showDisableDialog = true
                                }
                            },
                            enabled = true // Always enabled for user interaction
                        )
                    }
                }

                // Dialogs (Logic only, UI is triggered by state)
                item {
                    if (showEnableDialog) {
                        AlertDialog(
                            onDismissRequest = { showEnableDialog = false },
                            title = { Text("Enable CallSecure", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                            text = {
                                Text("To enable CallSecure, you must confirm. Normal mode will be set by default.", color = MaterialTheme.colorScheme.onSurface)
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            confirmButton = {
                                Button(onClick = {
                                    viewModel.toggleAppEnabled()
                                    viewModel.setMode(CallMode.NORMAL)
                                    callModesViewModel.createOrActivateMode("NORMAL")
                                    showEnableDialog = false
                                    CallSecureApp.isCallShieldEnabled = true
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                Button(onClick = { showEnableDialog = false }) { Text("Cancel") }
                            },
                            modifier = Modifier.fillMaxWidth(0.95f).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge)
                        )
                    }
                    
                    if (showDisableDialog) {
                        AlertDialog(
                            onDismissRequest = { showDisableDialog = false },
                            title = { Text("Disable CallSecure", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                            text = {
                                Text("You are disabling the switch. Your phone will receive ALL CALLS WITHOUT ANY CALL FILTERING, including unknown calls. Press OK to disable CallSecure.", color = MaterialTheme.colorScheme.onSurface)
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            confirmButton = {
                                Button(onClick = {
                                    viewModel.toggleAppEnabled()
                                    showDisableDialog = false
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                Button(onClick = { showDisableDialog = false }) { Text("Cancel") }
                            },
                            modifier = Modifier.fillMaxWidth(0.95f).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge)
                        )
                    }

                    if (showModeChangeDialog && pendingMode != null) {
                        AlertDialog(
                            onDismissRequest = {
                                showModeChangeDialog = false
                                pendingMode = null
                            },
                            title = { Text("Change Call Mode", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                            text = {
                                Text("CallSecure will be applied to the selected mode. Press OK to switch mode.", color = MaterialTheme.colorScheme.onSurface)
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            confirmButton = {
                                Button(onClick = {
                                    viewModel.setMode(pendingMode!!)
                                    val modeName = when (pendingMode) {
                                        CallMode.FAMILY -> "FAMILY"
                                        CallMode.EMERGENCY -> "EMERGENCY"
                                        else -> "NORMAL"
                                    }
                                    val modeEntity = modes.find { it.name.equals(modeName, ignoreCase = true) }
                                    if (modeEntity != null) {
                                        callModesViewModel.setActiveMode(modeEntity.id)
                                    }
                                    lastSelectedMode = pendingMode
                                    showModeChangeDialog = false
                                    pendingMode = null
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                Button(onClick = {
                                    showModeChangeDialog = false
                                    pendingMode = null
                                }) { Text("Cancel") }
                            },
                            modifier = Modifier.fillMaxWidth(0.95f).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Premium Badge
                if (isPremium) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Text(
                                "✨ Premium Features Unlocked",
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Mode Selection
                item {
                    Text(
                        "Call Mode",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (currentMode == null) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        ModeSelector(
                            currentMode!!,
                            windowSizeClass = windowSizeClass,
                            enabled = isAppEnabled,
                            normalEnabled = isAppEnabled,
                            familyEnabled = isAppEnabled && enabledModes["FAMILY"] == true,
                            emergencyEnabled = isAppEnabled && enabledModes["EMERGENCY"] == true
                        ) { newMode ->
                            if (!isAppEnabled) return@ModeSelector
                            if (lastSelectedMode != newMode) {
                                pendingMode = newMode
                                showModeChangeDialog = true
                            }
                        }
                    }
                }

                // NEW: Activity Overview Section
                item {
                    ActivityOverviewSection(viewModel)
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ActivityOverviewSection(viewModel: DashboardViewModel) {
    val totalArrivals by viewModel.totalArrivals.collectAsState()
    val answeredCalls by viewModel.answeredCalls.collectAsState()
    val blocked by viewModel.blocked.collectAsState()
    
    val receivedTrends by viewModel.receivedTrends.collectAsState()
    val allowedTrends by viewModel.allowedTrends.collectAsState()
    val blockedTrends by viewModel.blockedTrends.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Standard 4-column row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ActivityStatCard(
                label = "Received",
                value = totalArrivals.toString(),
                painter = painterResource(id = R.drawable.ic_mode_normal),
                iconTint = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
            ActivityStatCard(
                label = "Allowed",
                value = answeredCalls.toString(),
                painter = painterResource(id = R.drawable.ic_mode_family),
                iconTint = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            ActivityStatCard(
                label = "Blocked",
                value = blocked.toString(),
                painter = painterResource(id = R.drawable.ic_blocked_call),
                iconTint = Color(0xFFEF4444),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Activity Overview Chart Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Activity Overview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Today", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                DashboardTrendsGraph(
                    receivedTrends = receivedTrends,
                    allowedTrends = allowedTrends,
                    blockedTrends = blockedTrends
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Legend (2x2 Grid for perfect alignment)
                // Legend Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem("Received", Color(0xFF3B82F6))
                    LegendItem("Allowed", Color(0xFF10B981))
                    LegendItem("Blocked", Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
private fun DashboardTrendsGraph(
    receivedTrends: List<Pair<String, Int>>,
    allowedTrends: List<Pair<String, Int>>,
    blockedTrends: List<Pair<String, Int>>
) {
    val surfaceColor = Color.Transparent.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    
    val colors = listOf(
        Color(0xFF3B82F6), // Received
        Color(0xFF10B981), // Allowed
        Color(0xFFEF4444)  // Blocked
    )

    AndroidView(
        factory = { ctx: Context ->
            LineChart(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    300
                )
                setBackgroundColor(surfaceColor)
                description.isEnabled = false
                axisRight.isEnabled = false
                axisLeft.apply {
                    textColor = onSurfaceColor
                    setDrawGridLines(true)
                    gridColor = onSurfaceColor
                    gridLineWidth = 0.5f
                    axisMinimum = 0f
                }
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = onSurfaceColor
                    setDrawGridLines(false)
                    granularity = 4f
                }
                legend.isEnabled = false
            }
        },
        update = { chart: LineChart ->
            val dataSets = mutableListOf<LineDataSet>()
            
            val allTrends = listOf(receivedTrends, allowedTrends, blockedTrends)
            val labels = listOf("Received", "Allowed", "Blocked")
            
            allTrends.forEachIndexed { index, trends ->
                val entries = trends.mapIndexed { idx, pair ->
                    Entry(idx.toFloat(), pair.second.toFloat())
                }
                val dataSet = LineDataSet(entries, labels[index]).apply {
                    color = colors[index].toArgb()
                    setCircleColor(colors[index].toArgb())
                    lineWidth = 2f
                    circleRadius = 2.5f
                    setDrawValues(false)
                    setDrawFilled(index == 0) // Only fill the main "Received" set
                    if (index == 0) {
                        fillColor = colors[index].copy(alpha = 0.1f).toArgb()
                    }
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }
                dataSets.add(dataSet)
            }
            
            chart.data = LineData(dataSets.toList())
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(receivedTrends.map { it.first })
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}

@Composable
private fun LegendItem(label: String, color: Color, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ModeSelector(
    currentMode: CallMode,
    windowSizeClass: WindowSizeClass,
    enabled: Boolean,
    normalEnabled: Boolean = false,
    familyEnabled: Boolean = false,
    emergencyEnabled: Boolean = false,
    onModeSelected: (CallMode) -> Unit
) {
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    
    if (isCompact) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModeButton(
                label = "Normal",
                iconRes = R.drawable.ic_mode_normal,
                isSelected = currentMode == CallMode.NORMAL,
                enabled = enabled && normalEnabled,
                selectedColor = MaterialTheme.colorScheme.primary,
                onClick = { onModeSelected(CallMode.NORMAL) }
            )
            ModeButton(
                label = "Family",
                iconRes = R.drawable.ic_mode_family,
                isSelected = currentMode == CallMode.FAMILY,
                enabled = enabled && familyEnabled,
                selectedColor = com.akshaglobal.smartcallshield.presentation.ui.theme.PranixGreen,
                onClick = { onModeSelected(CallMode.FAMILY) }
            )
            ModeButton(
                label = "Emergency",
                iconRes = R.drawable.ic_mode_emergency,
                isSelected = currentMode == CallMode.EMERGENCY,
                enabled = enabled && emergencyEnabled,
                selectedColor = MaterialTheme.colorScheme.error,
                onClick = { onModeSelected(CallMode.EMERGENCY) }
            )
        }
    } else {
        // Single row for wider screens as well, since there are only 3 modes now
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            ModeButton(
                label = "Normal",
                iconRes = R.drawable.ic_mode_normal,
                isSelected = currentMode == CallMode.NORMAL,
                enabled = enabled && normalEnabled,
                selectedColor = MaterialTheme.colorScheme.primary,
                onClick = { onModeSelected(CallMode.NORMAL) }
            )
            Spacer(Modifier.width(16.dp))
            ModeButton(
                label = "Family",
                iconRes = R.drawable.ic_mode_family,
                isSelected = currentMode == CallMode.FAMILY,
                enabled = enabled && familyEnabled,
                selectedColor = com.akshaglobal.smartcallshield.presentation.ui.theme.PranixGreen,
                onClick = { onModeSelected(CallMode.FAMILY) }
            )
            Spacer(Modifier.width(16.dp))
            ModeButton(
                label = "Emergency",
                iconRes = R.drawable.ic_mode_emergency,
                isSelected = currentMode == CallMode.EMERGENCY,
                enabled = enabled && emergencyEnabled,
                selectedColor = MaterialTheme.colorScheme.error,
                onClick = { onModeSelected(CallMode.EMERGENCY) }
            )
        }
    }
}
