package com.akshaglobal.smartcallshield.presentation.ui.screens

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.akshaglobal.smartcallshield.presentation.viewmodel.AnalyticsViewModel
import com.akshaglobal.smartcallshield.presentation.viewmodel.TrendFilter
import com.akshaglobal.smartcallshield.presentation.viewmodel.SpamReportViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import androidx.compose.ui.graphics.toArgb
import com.akshaglobal.smartcallshield.presentation.ui.components.AnalyticsMetricCard
import com.akshaglobal.smartcallshield.common_ui.components.InsightCard
import com.akshaglobal.smartcallshield.common_ui.components.StatBox

@Composable
fun AnalyticsScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: AnalyticsViewModel = hiltViewModel(),
    spamReportViewModel: SpamReportViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val blockedCalls by viewModel.blockedCalls.collectAsState()
    val spamCallsPrevented by viewModel.spamCallsPrevented.collectAsState()

    LaunchedEffect(Unit) {
        spamReportViewModel.observeSpamReportsCount()
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
                .widthIn(max = 600.dp)
        ) {
            // Header
            Text(
                "Analytics Dashboard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(16.dp)
            )

            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Overview") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Trends") }
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> OverviewTab(
                        blockedCalls,
                        spamCallsPrevented
                    )
                    1 -> TrendsTab(viewModel, windowSizeClass)
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(
    blockedCalls: Long,
    spamCallsPrevented: Long
) {
    Text(
        "This Month",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    AnalyticsMetricCard(
        title = "Total Blocked Calls",
        value = blockedCalls.toString(),
        unit = "calls",
        backgroundColor = MaterialTheme.colorScheme.errorContainer,
        valueColor = MaterialTheme.colorScheme.onErrorContainer
    )

    Spacer(modifier = Modifier.height(12.dp))

    AnalyticsMetricCard(
        title = "Spam Calls Prevented",
        value = spamCallsPrevented.toString(),
        unit = "calls",
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        valueColor = MaterialTheme.colorScheme.onPrimaryContainer
    )

    Spacer(modifier = Modifier.height(24.dp))

   Spacer(modifier = Modifier.height(12.dp))

    Text(
        "Insights",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    InsightCard(
        icon = "📊",
        title = "Peak Hours",
        description = "Most calls occur between 2-4PM make use of the app properly"
    )

    Spacer(modifier = Modifier.height(12.dp))

    InsightCard(
        icon = "🔒",
        title = "Privacy Protected",
        description = "All data processed locally on device"
    )
}

@Composable
private fun TrendsTab(viewModel: AnalyticsViewModel, windowSizeClass: WindowSizeClass) {
    val callTrends by viewModel.callTrends.collectAsState()
    val trendFilter by viewModel.trendFilter.collectAsState()
    val totalArrivals by viewModel.totalArrivals.collectAsState()
    val answeredCalls by viewModel.answeredCalls.collectAsState()
    val blocked by viewModel.blocked.collectAsState()

    val filters = listOf(
        TrendFilter.TODAY to "Today",
        TrendFilter.WEEK to "Week",
        TrendFilter.MONTH to "Month",
        TrendFilter.OVERALL to "Overall"
    )

    Column {
        Text(
            "Call Trends",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        // Filter chips
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            filters.forEach { (filter: TrendFilter, label: String) ->
                androidx.compose.material3.FilterChip(
                    selected = trendFilter == filter,
                    onClick = { viewModel.setTrendFilter(filter) },
                    label = { Text(label) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        // Adaptive Statistics Grid
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatBox("Total Arrival", totalArrivals, Modifier.weight(1f))
                StatBox("Answered", answeredCalls, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatBox("Blocked", blocked, Modifier.weight(1f))
                Spacer(Modifier.weight(1f))
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatBox("Total Arrival", totalArrivals, Modifier.weight(1f))
                StatBox("Answered", answeredCalls, Modifier.weight(1f))
                StatBox("Blocked", blocked, Modifier.weight(1f))
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        // Call trends graph (MPAndroidChart integration)
        MPAndroidChartTrendsGraph(callTrends)
    }
}

// --- MPAndroidChart Integration ---
@Composable
private fun MPAndroidChartTrendsGraph(callTrends: List<Pair<String, Int>>) {
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val primaryAlphaColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f).toArgb()

    AndroidView(
        factory = { ctx: Context ->
            LineChart(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    400 // px, will be scaled by Compose
                )
                setBackgroundColor(surfaceColor)
                description.isEnabled = false
                axisRight.isEnabled = false
                axisLeft.isEnabled = true
                axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                axisLeft.textColor = onSurfaceColor
                axisLeft.setTextSize(10f)
                axisLeft.setXOffset(12f)
                axisLeft.setSpaceTop(10f)
                axisLeft.setSpaceBottom(10f)
                axisLeft.setDrawGridLines(true)
                axisLeft.setDrawAxisLine(false)
                axisLeft.setLabelCount(6, true)
                axisLeft.setGranularity(0.2f)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = onSurfaceColor
                xAxis.setDrawGridLines(false)
                xAxis.setAvoidFirstLastClipping(true)
                xAxis.setLabelCount(callTrends.size.coerceAtMost(6), true)
                setExtraOffsets(18f, 8f, 12f, 20f)
                setViewPortOffsets(24f, 8f, 10f, 16f)
                minOffset = 18f
                legend.isEnabled = false
            }
        },
        update = { chart: LineChart ->
            val entries = callTrends.mapIndexed { idx, pair ->
                Entry(idx.toFloat(), pair.second.toFloat())
            }
            val dataSet = LineDataSet(entries, "Calls").apply {
                color = primaryColor
                setCircleColor(primaryColor)
                lineWidth = 2f
                circleRadius = 4f
                setDrawValues(false)
                setDrawFilled(true)
                fillColor = primaryAlphaColor
            }
            chart.data = LineData(dataSet)
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(callTrends.map { it.first })
            chart.xAxis.labelRotationAngle = -45f
            chart.xAxis.textColor = onSurfaceColor
            chart.axisLeft.textColor = onSurfaceColor
            chart.setBackgroundColor(surfaceColor)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    )
}
