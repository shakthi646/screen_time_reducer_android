package com.ksp.screentimereducer.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ksp.screentimereducer.presentation.main.MainShell
import com.ksp.screentimereducer.presentation.navigation.Routes
import com.ksp.screentimereducer.presentation.onboarding.OnboardingScreen
import com.ksp.screentimereducer.presentation.onboarding.OnboardingViewModel

@Composable
fun RootNavigator() {
    val navController: NavHostController = rememberNavController()
    val gateViewModel: OnboardingViewModel = hiltViewModel()
    val state by gateViewModel.state.collectAsStateWithLifecycle()

    val startDestination = when {
        state.loading -> null
        state.onboardingComplete -> Routes.ROOT
        else -> Routes.ONBOARDING
    }

    // We render an empty surface while the preferences flow has yet to
    // emit; this is typically a single frame and prevents a flash of the
    // wrong start destination.
    if (startDestination == null) return

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.ROOT) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ROOT) { MainShell() }
    }
}
