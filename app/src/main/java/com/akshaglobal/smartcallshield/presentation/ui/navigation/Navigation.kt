package com.akshaglobal.smartcallshield.presentation.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.akshaglobal.smartcallshield.presentation.ui.screens.AnalyticsScreen
import com.akshaglobal.smartcallshield.presentation.ui.screens.CallModesManagementScreen
import com.akshaglobal.smartcallshield.presentation.ui.screens.ContactsScreen
import com.akshaglobal.smartcallshield.presentation.ui.screens.DashboardScreen
import com.akshaglobal.smartcallshield.presentation.ui.screens.SettingsScreen
import com.akshaglobal.smartcallshield.presentation.ui.components.BannerAdView
import androidx.hilt.navigation.compose.hiltViewModel
import com.akshaglobal.smartcallshield.presentation.viewmodel.DashboardViewModel

sealed class Screen(val route: String, val label: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object Contacts : Screen("contacts", "Contacts")
    object Analytics : Screen("analytics", "Analytics")
    object Settings : Screen("settings", "Settings")
    object CallModes : Screen("callmodes", "Call Modes")
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    // Shared DashboardViewModel for app state
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val isAppEnabled by dashboardViewModel.isAppEnabled.collectAsState()
    Scaffold(
        bottomBar = {
            Column {
                BannerAdView()
                NavigationBar {
                    val items = listOf(
                        Screen.Dashboard,
                        Screen.Contacts,
                        Screen.Analytics,
                        Screen.Settings
                    )
    
                    items.forEachIndexed { index, screen ->
                        val icon = when (screen) {
                            Screen.Dashboard -> Icons.Default.Home
                            Screen.Contacts -> Icons.Default.Phone
                            Screen.Analytics -> Icons.Default.Info
                            Screen.Settings -> Icons.Default.Settings
                            else -> Icons.Default.Settings
                        }
    
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = dashboardViewModel)
            }
            composable(Screen.Contacts.route) {
                ContactsScreen()
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(isAppEnabled = isAppEnabled)
            }
            composable(Screen.CallModes.route) {
                CallModesManagementScreen()
            }
        }
    }
}
