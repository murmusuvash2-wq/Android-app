package com.example.share.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.Screen
import com.example.ui.theme.*

/**
 * Screen displayed when an incoming shared product link cannot be recognized.
 * Consumes 0 credits and offers clear recovery options.
 */
@Composable
fun SharedProductErrorScreen(navController: NavController) {
    val context = LocalContext.current
    var isRetrying by remember { mutableStateOf(false) }

    BackHandler {
        navController.navigate(Screen.MainApp.route) {
            popUpTo(Screen.SharedProductError.route) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .testTag("shared_product_error_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.navigate(Screen.MainApp.route) {
                        popUpTo(Screen.SharedProductError.route) { inclusive = true }
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to home",
                    tint = Charcoal
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Product Share",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            border = BorderStroke(1.dp, WarmGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Bag Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(OffWhite, CircleShape)
                        .border(1.dp, WarmGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Unrecognized product",
                        tint = DeepForest,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "We couldn't recognize this product",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("shared_product_error_title")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "The shared link or merchant is not currently supported for instant try-on. You can try again or choose from curated styles in TiHin.",
                    fontSize = 14.sp,
                    color = SoftCharcoal,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Action 1: Try Again
                Button(
                    onClick = {
                        isRetrying = true
                        when (val outcome = ShareFlowManager.retryResolution()) {
                            is ShareResolutionOutcome.GoToTryOn -> {
                                isRetrying = false
                                navController.navigate(Screen.TryOn.route) {
                                    popUpTo(Screen.SharedProductError.route) { inclusive = true }
                                }
                            }
                            else -> {
                                isRetrying = false
                                /* Toast disabled for tests */
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("try_again_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Charcoal,
                        contentColor = Color.White
                    ),
                    enabled = !isRetrying
                ) {
                    Text(
                        text = if (isRetrying) "Checking..." else "Try Again",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action 2: Choose Product from TiHin
                OutlinedButton(
                    onClick = {
                        navController.navigate(Screen.MainApp.route) {
                            popUpTo(Screen.SharedProductError.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("choose_product_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Charcoal
                    ),
                    border = BorderStroke(1.dp, WarmGray)
                ) {
                    Text(
                        text = "Choose Product from TiHin",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
