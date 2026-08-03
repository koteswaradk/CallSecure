package com.akshaglobal.smartcallshield.presentation.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.navigation.NavHostController
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
fun MainNavigation(
    windowSizeClass: WindowSizeClass,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val isAppEnabled by dashboardViewModel.isAppEnabled.collectAsState()
    val navController = rememberNavController()

    MainNavigationContent(
        navController = navController,
        isAppEnabled = isAppEnabled,
        windowSizeClass = windowSizeClass,
        dashboardContent = { DashboardScreen(windowSizeClass = windowSizeClass, viewModel = dashboardViewModel) }
    )
}

@Composable
fun MainNavigationContent(
    navController: NavHostController,
    isAppEnabled: Boolean,
    windowSizeClass: WindowSizeClass,
    dashboardContent: @Composable () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    AdaptiveBottomBarContent(
        navController = navController,
        isAppEnabled = isAppEnabled,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        windowSizeClass = windowSizeClass,
        dashboardContent = dashboardContent
    )
}

@Composable
private fun AdaptiveBottomBarContent(
    navController: NavHostController,
    isAppEnabled: Boolean,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    windowSizeClass: WindowSizeClass,
    dashboardContent: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            Column {
                BannerAdView(isCollapsible = true)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background
                ) {
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
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            ),
                            onClick = {
                                onTabSelected(index)
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
        NavHostContent(
            navController = navController,
            paddingValues = paddingValues,
            isAppEnabled = isAppEnabled,
            windowSizeClass = windowSizeClass,
            dashboardContent = dashboardContent
        )
    }
}


@Composable
private fun NavHostContent(
    navController: NavHostController,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    isAppEnabled: Boolean,
    windowSizeClass: WindowSizeClass,
    dashboardContent: @Composable () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.Dashboard.route) {
            dashboardContent()
        }
        composable(Screen.Contacts.route) {
            ContactsScreen()
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen(windowSizeClass = windowSizeClass)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(windowSizeClass = windowSizeClass, isAppEnabled = isAppEnabled)
        }
        composable(Screen.CallModes.route) {
            CallModesManagementScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun MainNavigationPreview() {
    val windowSizeClass = rememberWindowSizeClass()
    com.akshaglobal.smartcallshield.presentation.ui.theme.DriveShieldTheme {
        MainNavigationContent(
            navController = rememberNavController(),
            isAppEnabled = true,
            windowSizeClass = windowSizeClass,
            dashboardContent = {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dashboard Placeholder")
                }
            }
        )
    }
}

@ExperimentalMaterial3WindowSizeClassApi
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    // Helper for preview
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    return WindowSizeClass.calculateFromSize(
        androidx.compose.ui.unit.DpSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
    )
}
