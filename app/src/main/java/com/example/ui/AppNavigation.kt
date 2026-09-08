package com.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object MainApp : Screen("main_app")
    object TryOn : Screen("try_on")
    object Processing : Screen("processing")
    object Result : Screen("result")
    object Looks : Screen("looks")
    object TriesCredits : Screen("tries_credits")
}

@Composable
fun AppNavigation(startDestination: String = Screen.Splash.route) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
        composable(Screen.MainApp.route) { AppShell(navController) }
        composable(Screen.TryOn.route) { TryOnScreen(navController) }
        composable(Screen.Processing.route) { ProcessingScreen(navController) }
        composable(Screen.Result.route) { ResultScreen(navController) }
        composable(Screen.Looks.route) { LooksScreen(navController) }
        composable(Screen.TriesCredits.route) { CreditStoreScreen(navController) }
    }
}
