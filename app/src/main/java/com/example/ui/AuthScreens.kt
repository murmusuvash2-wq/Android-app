package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.BorderColor
import com.example.ui.theme.DeepForest
import com.example.ui.theme.EditorialSerif
import com.example.ui.theme.Inter
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PurchaseCTA
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftCharcoal
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.SurfaceVariantColor
import com.example.ui.theme.WarmIvory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { Animatable(0.96f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(1800)
        navController.navigate(Screen.Onboarding.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.scale(scale.value)
            ) {
                TiHinLogo(
                    textSize = 46.sp,
                    showSparkle = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Try. Love. Buy.",
                fontFamily = Inter,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SoftCharcoal,
                letterSpacing = 2.2.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(DeepForest.copy(alpha = 0.55f))
                    )
                }
            }
        }
    }
}

data class OnboardingSlide(
    val title: String,
    val eyebrow: String,
    val subtitle: String,
    val icon: ImageVector,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val slides = listOf(
        OnboardingSlide(
            title = "Try.",
            eyebrow = "SEE IT ON YOU",
            subtitle = "See it on you before you buy.",
            icon = Icons.Default.Visibility,
            imageUrl = "https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=1200&q=85"
        ),
        OnboardingSlide(
            title = "Love.",
            eyebrow = "FIND YOUR STYLE",
            subtitle = "Find your perfect look with confidence.",
            icon = Icons.Default.Favorite,
            imageUrl = "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=1200&q=85"
        ),
        OnboardingSlide(
            title = "Buy.",
            eyebrow = "SHOP YOUR FAVOURITES",
            subtitle = "Shop your favourite styles in just a tap.",
            icon = Icons.Default.ShoppingBag,
            imageUrl = "https://images.unsplash.com/photo-1525507119028-ed4c629a60a3?auto=format&fit=crop&w=1200&q=85"
        )
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val coroutineScope = rememberCoroutineScope()
    var showAuthSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("onboarding_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TiHinLogo(textSize = 22.sp, showSparkle = true)

                TextButton(
                    onClick = { showAuthSheet = true },
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = "Skip",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SoftCharcoal
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val slide = slides[page]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(SurfaceVariantColor)
                    ) {
                        AsyncImage(
                            model = slide.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // A subtle ivory veil keeps the editorial typography readable
                        // without hiding the fashion photography.
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SurfaceVariantColor.copy(alpha = 0.16f))
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(WarmIvory.copy(alpha = 0.88f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = slide.icon,
                                contentDescription = null,
                                tint = DeepForest,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        Text(
                            text = slide.eyebrow,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(18.dp),
                            fontFamily = Inter,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp,
                            color = DeepForest
                        )

                        Text(
                            text = slide.title,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp),
                            fontFamily = EditorialSerif,
                            fontSize = 46.sp,
                            lineHeight = 46.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = slide.subtitle,
                        fontFamily = Inter,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        color = PrimaryText,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(slides.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 22.dp else 7.dp, 7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (selected) DeepForest else BorderColor)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                if (pagerState.currentPage == slides.size - 1) {
                    Button(
                        onClick = { showAuthSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("onboarding_get_started_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurchaseCTA,
                            contentColor = SurfaceColor
                        )
                    ) {
                        Text("Get Started", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("onboarding_next_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepForest,
                                contentColor = SurfaceColor
                            )
                        ) {
                            Text("Next", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.size(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showAuthSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showAuthSheet = false },
                sheetState = sheetState,
                containerColor = SurfaceColor,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(BorderColor)
                    )
                }
            ) {
                AuthBottomSheetContent(
                    onDismiss = { showAuthSheet = false },
                    onAuthSuccess = {
                        showAuthSheet = false
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AuthBottomSheetContent(
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp)
            .navigationBarsPadding()
            .testTag("auth_bottom_sheet"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isSignUp) "Create Account" else "Welcome to TiHin",
                fontFamily = EditorialSerif,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = SecondaryText)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                SessionManager.loginWithGoogle()
                onAuthSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("auth_google_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SurfaceVariantColor,
                contentColor = PrimaryText
            )
        ) {
            Text("Continue with Google", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(BorderColor)
            )
            Text(
                text = "or",
                modifier = Modifier.padding(horizontal = 12.dp),
                fontSize = 12.sp,
                color = SecondaryText
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(BorderColor)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_email_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_password_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                SessionManager.loginWithEmail(email)
                onAuthSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("auth_submit_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PurchaseCTA,
                contentColor = SurfaceColor
            )
        ) {
            Text(
                text = if (isSignUp) "Sign Up" else "Sign In",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { isSignUp = !isSignUp }
        ) {
            Text(
                text = if (isSignUp) "Already have an account? Sign In" else "New to TiHin? Create account",
                fontSize = 13.sp,
                color = DeepForest
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                SessionManager.continueAsGuest()
                onAuthSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("auth_guest_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Explore as Guest", fontSize = 13.sp, color = SecondaryText)
        }
    }
}
