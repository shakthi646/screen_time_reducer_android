package com.ksp.screentimereducer.presentation.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ksp.screentimereducer.R
import com.ksp.screentimereducer.presentation.challenges.ChallengesScreen
import com.ksp.screentimereducer.presentation.dashboard.DashboardScreen
import com.ksp.screentimereducer.presentation.focus.FocusScreen
import com.ksp.screentimereducer.presentation.limits.LimitsScreen
import com.ksp.screentimereducer.presentation.navigation.Routes
import com.ksp.screentimereducer.presentation.settings.SettingsScreen

private data class NavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

private val NAV_ITEMS = listOf(
    NavItem(Routes.DASHBOARD, R.string.nav_dashboard, Icons.Rounded.Home),
    NavItem(Routes.FOCUS, R.string.nav_focus, Icons.Rounded.Timer),
    NavItem(Routes.LIMITS, R.string.nav_limits, Icons.Rounded.AccessTime),
    NavItem(Routes.CHALLENGES, R.string.nav_challenges, Icons.Rounded.AutoAwesome),
    NavItem(Routes.SETTINGS, R.string.nav_settings, Icons.Rounded.Settings),
)

@Composable
fun MainShell() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                NAV_ITEMS.forEach { item ->
                    val selected = backStackEntry?.destination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.DASHBOARD) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.labelRes)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    )
                }
            }
        },
    ) { innerPadding: PaddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.DASHBOARD) { DashboardScreen() }
            composable(Routes.FOCUS) { FocusScreen() }
            composable(Routes.LIMITS) { LimitsScreen() }
            composable(Routes.CHALLENGES) { ChallengesScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
        }
    }
}

