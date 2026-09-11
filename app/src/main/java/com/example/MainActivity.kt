package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.share.ui.ShareFlowManager
import com.example.share.ui.ShareResolutionOutcome
import com.example.ui.AppNavigation
import com.example.ui.AppPersistence
import com.example.ui.Screen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private var activeNavController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppPersistence.init(this)

        var startDest = Screen.Splash.route
        if (intent?.action == Intent.ACTION_SEND) {
            startDest = when (ShareFlowManager.processIntent(intent, this)) {
                is ShareResolutionOutcome.GoToTryOn,
                is ShareResolutionOutcome.GoToImageTryOn -> Screen.TryOn.route
                is ShareResolutionOutcome.ShowError -> Screen.SharedProductError.route
                is ShareResolutionOutcome.None -> Screen.Splash.route
            }
        }

        setContent {
            val navController = rememberNavController()
            DisposableEffect(navController) {
                activeNavController = navController
                onDispose {
                    activeNavController = null
                }
            }

            MyApplicationTheme {
                AppNavigation(startDestination = startDest, navController = navController)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return

        val outcome = ShareFlowManager.processIntent(intent, this)
        val navController = activeNavController ?: return

        when (outcome) {
            is ShareResolutionOutcome.GoToTryOn,
            is ShareResolutionOutcome.GoToImageTryOn -> {
                navController.navigate(Screen.TryOn.route) {
                    launchSingleTop = true
                }
            }
            is ShareResolutionOutcome.ShowError -> {
                navController.navigate(Screen.SharedProductError.route) {
                    launchSingleTop = true
                }
            }
            is ShareResolutionOutcome.None -> {
                // Not a share intent
            }
        }
    }
}
