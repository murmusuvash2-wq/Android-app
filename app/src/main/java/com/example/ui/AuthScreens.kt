package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(1800)
        navController.navigate(Screen.Onboarding.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "TiHin",
                fontWeight = FontWeight.Bold,
                fontSize = 46.sp,
                color = Charcoal,
                letterSpacing = (-1.5).sp
            )
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = DeepForest,
                modifier = Modifier
                    .size(24.dp)
                    .offset(x = 4.dp, y = (-12).dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.tagline),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = SoftCharcoal
        )
        Spacer(modifier = Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DeepForest.copy(alpha = alpha))
        )
    }
}

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val tag: String,
    @DrawableRes val drawableRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "Try.",
                subtitle = "See it on you before you buy.",
                tag = "try",
                drawableRes = R.drawable.tihin_onboarding_try
            ),
            OnboardingPageData(
                title = "Love.",
                subtitle = "Find your perfect look with confidence.",
                tag = "love",
                drawableRes = R.drawable.tihin_onboarding_love
            ),
            OnboardingPageData(
                title = "Buy.",
                subtitle = "Shop your favourite styles in just a tap.",
                tag = "buy",
                drawableRes = R.drawable.tihin_onboarding_buy
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("onboarding_pager")
        ) { page ->
            val item = pages[page]
            OnboardingNativePage(
                item = item,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp, end = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                onClick = { showBottomSheet = true },
                shape = RoundedCornerShape(20.dp),
                color = SurfaceColor.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, BorderColor.copy(alpha = 0.7f)),
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = "Skip",
                    color = Charcoal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Bottom Controls: Carousel Indicators and Next / Get Started Button
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isSelected) 24.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Charcoal else BorderColor)
                    )
                }
            }

            // Next / Get Started Button
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        showBottomSheet = true
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Charcoal,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = WarmIvory,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = SoftCharcoal.copy(alpha = 0.3f),
                    width = 40.dp,
                    height = 4.dp
                )
            }
        ) {
            AuthBottomSheetContent(
                onGoogleSignIn = {
                    SessionManager.isGuest = false
                    
                    showBottomSheet = false
                    navController.navigate(Screen.MainApp.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onGuestSignIn = {
                    SessionManager.isGuest = true
                    
                    showBottomSheet = false
                    navController.navigate(Screen.MainApp.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
internal fun OnboardingNativePage(
    item: OnboardingPageData,
    modifier: Modifier = Modifier
) {
    if (item.drawableRes != 0) {
        // Supplied custom artwork presentation:
        // Complete editorial composition (heading, subtitle, main visual, TiHin branding).
        // Occupies the central onboarding area with responsive width-driven sizing and comfortable control clearance.
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(WarmIvory)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    top = 58.dp,
                    bottom = 86.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.drawableRes),
                contentDescription = "${item.title} ${item.subtitle}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 480.dp)
                    .testTag("onboarding_custom_image_${item.tag}")
            )
        }
    } else {
        // Fallback UI ONLY when custom artwork is genuinely unavailable (drawableRes == 0)
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(WarmIvory)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 58.dp, bottom = 92.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.title,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        modifier = Modifier.testTag("onboarding_fallback_title_${item.tag}")
                    )
                    if (item.tag == "love") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFD48B7B),
                            modifier = Modifier.size(28.dp)
                        )
                    } else if (item.tag == "buy") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = DeepForest,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.subtitle,
                    style = Typography.bodyLarge,
                    color = SoftCharcoal,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.testTag("onboarding_fallback_subtitle_${item.tag}")
                )
            }

            // Fallback Graphic Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfaceColor,
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp, max = 340.dp)
                    .padding(vertical = 16.dp)
                    .testTag("onboarding_fallback_card_${item.tag}")
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (item.tag) {
                                "love" -> Icons.Default.Favorite
                                "buy" -> Icons.Default.ShoppingBag
                                else -> Icons.Default.Check
                            },
                            contentDescription = null,
                            tint = Charcoal.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = item.title,
                            style = Typography.titleMedium,
                            color = Charcoal
                        )
                    }
                }
            }
        }
    }
}

enum class AuthState { MAIN, SIGN_UP, FORGOT_PASSWORD }

@Composable
fun AuthBottomSheetContent(
    onGoogleSignIn: () -> Unit,
    onGuestSignIn: () -> Unit
) {
    var authState by remember { mutableStateOf(AuthState.MAIN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var showResetSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Crossfade(targetState = authState, label = "authCrossfade") { state ->
            when (state) {
                AuthState.MAIN -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Welcome to TiHin",
                            style = Typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sign in to save your tries and track prices.",
                            style = Typography.bodyMedium,
                            color = SoftCharcoal,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        Button(
                            onClick = onGoogleSignIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OffWhite, contentColor = Charcoal),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmGray)
                        ) {
                            Text("Continue with Google", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { authState = AuthState.SIGN_UP },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White)
                        ) {
                            Text("Sign Up with Email", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        TextButton(onClick = onGuestSignIn) {
                            Text("Continue as Guest", fontWeight = FontWeight.Medium, color = SoftCharcoal, fontSize = 15.sp)
                        }
                    }
                }
                AuthState.SIGN_UP -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Create Account",
                            style = Typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = false
                            },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = emailError,
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                passwordError = false
                            },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = passwordError,
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        if (passwordError) {
                            Text(
                                "Password must be at least 6 characters.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                                val isPasswordValid = password.length >= 6
                                if (!isEmailValid) emailError = true
                                if (!isPasswordValid) passwordError = true
                                if (isEmailValid && isPasswordValid) {
                                    onGoogleSignIn()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White)
                        ) {
                            Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        TextButton(onClick = { authState = AuthState.FORGOT_PASSWORD }) {
                            Text("Forgot Password?", color = SoftCharcoal, fontWeight = FontWeight.Medium)
                        }
                        TextButton(onClick = { authState = AuthState.MAIN }) {
                            Text("Back", color = SoftCharcoal, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                AuthState.FORGOT_PASSWORD -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Reset Password",
                            style = Typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        if (showResetSuccess) {
                            Text(
                                text = "Reset link sent! Please check your email.",
                                color = DeepForest,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        } else {
                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = false
                                },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = emailError,
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                        showResetSuccess = true
                                    } else {
                                        emailError = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White)
                            ) {
                                Text("Send Reset Link", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                        TextButton(onClick = { authState = AuthState.MAIN }) {
                            Text("Back", color = SoftCharcoal, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
