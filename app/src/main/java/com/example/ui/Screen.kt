package com.example.ui

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Discover : Screen("discover")
    data object Looks : Screen("looks")
    data object Me : Screen("me")
    data object TryOn : Screen("try_on")
    data object Processing : Screen("processing")
    data object Result : Screen("result")
    data object CreditStore : Screen("credit_store")
    data object TriesCredits : Screen("credit_store")
    data object SharedProductError : Screen("shared_product_error")
}
