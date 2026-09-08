package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
                text = "OnMe",
                fontWeight = FontWeight.Bold,
                fontSize = 46.sp,
                color = Lavender,
                letterSpacing = (-1.5).sp
            )
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Lavender,
                modifier = Modifier
                    .size(24.dp)
                    .offset(x = 4.dp, y = (-12).dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AI Virtual Outfit Try-On",
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
                .background(Lavender.copy(alpha = alpha))
        )
    }
}

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "See it on you",
                subtitle = "Try on any outfit virtually with AI precision before you buy.",
                imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=1000&q=80"
            ),
            OnboardingPageData(
                title = "Discover your next look",
                subtitle = "Browse trending aesthetics, runway looks, and top brand drops.",
                imageUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=1000&q=80"
            ),
            OnboardingPageData(
                title = "Try, Save, Shop",
                subtitle = "Keep your favorite fitted looks and build your personal digital wardrobe.",
                imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=1000&q=80"
            ),
            OnboardingPageData(
                title = "Shop where you love",
                subtitle = "Get direct links to top merchants and track real-time price drops.",
                imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=1000&q=80"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize().background(WarmIvory)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = pages[page]
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Bottom Content
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 28.dp)
                        .padding(bottom = 120.dp)
                ) {
                    Text(
                        text = item.title,
                        style = Typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.subtitle,
                        style = Typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Top Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, end = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                onClick = {
                    showBottomSheet = true
                },
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.35f)
            ) {
                Text(
                    text = "Skip",
                    color = Color.White,
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
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isSelected) 24.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.4f))
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
                colors = ButtonDefaults.buttonColors(containerColor = Lavender, contentColor = Color.White)
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
                    SessionManager.credits = 12
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
                            text = "Welcome to OnMe",
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
                            colors = ButtonDefaults.buttonColors(containerColor = Lavender, contentColor = Color.White)
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
                            colors = ButtonDefaults.buttonColors(containerColor = Lavender, contentColor = Color.White)
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
                                color = Lavender,
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
                                colors = ButtonDefaults.buttonColors(containerColor = Lavender, contentColor = Color.White)
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
