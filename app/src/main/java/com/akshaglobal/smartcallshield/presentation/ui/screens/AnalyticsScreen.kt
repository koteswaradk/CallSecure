package com.akshaglobal.smartcallshield.presentation.ui.screens

import android.content.Context
import android.graphics.Color as AndroidColor
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.akshaglobal.smartcallshield.presentation.viewmodel.AnalyticsViewModel
import com.akshaglobal.smartcallshield.presentation.viewmodel.TrendFilter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val blockedCalls by viewModel.blockedCalls.collectAsState()
    val spamCallsPrevented by viewModel.spamCallsPrevented.collectAsState()
    val drivingRepliesSent by viewModel.drivingRepliesSent.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header
        Text(
            "Analytics Dashboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
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
                0 -> OverviewTab(blockedCalls, spamCallsPrevented, drivingRepliesSent)
                1 -> TrendsTab(viewModel)
            }
        }
    }
}

@Composable
private fun OverviewTab(
    blockedCalls: Long,
    spamCallsPrevented: Long,
    drivingRepliesSent: Long
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
        backgroundColor = Color(0xFFFFEBEE),
        valueColor = Color(0xFFC62828)
    )

    Spacer(modifier = Modifier.height(12.dp))

    AnalyticsMetricCard(
        title = "Spam Calls Prevented",
        value = spamCallsPrevented.toString(),
        unit = "calls",
        backgroundColor = Color(0xFFE8F5E9),
        valueColor = Color(0xFF2E7D32)
    )

    Spacer(modifier = Modifier.height(12.dp))

    AnalyticsMetricCard(
        title = "Driving Mode Auto-Replies",
        value = drivingRepliesSent.toString(),
        unit = "replies",
        backgroundColor = Color(0xFFE3F2FD),
        valueColor = Color(0xFF1565C0)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        "Insights",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    InsightCard(
        icon = "📊",
        title = "Peak Hours",
        description = "Most calls occur between 2-4 PM"
    )

    Spacer(modifier = Modifier.height(12.dp))

    InsightCard(
        icon = "🚗",
        title = "Driving Mode",
        description = "Active on average 45 minutes per day"
    )

    Spacer(modifier = Modifier.height(12.dp))

    InsightCard(
        icon = "🔒",
        title = "Privacy Protected",
        description = "All data processed locally on device"
    )
}

@Composable
private fun TrendsTab(viewModel: AnalyticsViewModel) {
    val callTrends by viewModel.callTrends.collectAsState()
    val trendFilter by viewModel.trendFilter.collectAsState()
    val totalArrivals by viewModel.totalArrivals.collectAsState()
    val answeredCalls by viewModel.answeredCalls.collectAsState()
    val blocked by viewModel.blocked.collectAsState()
    val autoReply by viewModel.autoReply.collectAsState()

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
        // Real-time statistics
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatBox("Total Arrival", totalArrivals, Modifier.weight(1f))
            StatBox("Answered", answeredCalls, Modifier.weight(1f))
            StatBox("Blocked", blocked, Modifier.weight(1f))
            StatBox("Auto-Reply", autoReply, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Call trends graph (MPAndroidChart integration)
        MPAndroidChartTrendsGraph(callTrends)
    }
}

// --- MPAndroidChart Integration ---
@Composable
private fun MPAndroidChartTrendsGraph(callTrends: List<Pair<String, Int>>) {
    AndroidView(
        factory = { ctx: Context ->
            LineChart(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    400 // px, will be scaled by Compose
                )
                setBackgroundColor(AndroidColor.WHITE)
                description.isEnabled = false
                axisRight.isEnabled = false
                axisLeft.textColor = AndroidColor.DKGRAY
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = AndroidColor.DKGRAY
                xAxis.setDrawGridLines(false)
                legend.isEnabled = false
            }
        },
        update = { chart: LineChart ->
            val entries = callTrends.mapIndexed { idx, pair ->
                Entry(idx.toFloat(), pair.second.toFloat())
            }
            val dataSet = LineDataSet(entries, "Calls").apply {
                color = "#1976D2".toColorInt()
                setCircleColor("#1976D2".toColorInt())
                lineWidth = 2f
                circleRadius = 4f
                setDrawValues(false)
                setDrawFilled(true)
                fillColor = "#BBDEFB".toColorInt()
            }
            chart.data = LineData(dataSet)
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(callTrends.map { it.first })
            chart.xAxis.labelRotationAngle = -45f
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color.White)
            .padding(8.dp)
    )
}

@Composable
private fun AnalyticsMetricCard(
    title: String,
    value: String,
    unit: String,
    backgroundColor: Color,
    valueColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontSize = 14.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = valueColor)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(unit, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun InsightCard(
    icon: String,
    title: String,
    description: String
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(description, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// --- MPAndroidChart Integration ---
@Composable
private fun StatBox(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
