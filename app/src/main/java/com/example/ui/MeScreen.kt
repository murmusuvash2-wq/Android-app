package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*

@Composable
fun MeScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var priceDropEnabled by remember { mutableStateOf(true) }
    var showAccountPrompt by remember { mutableStateOf(false) }

    if (showAccountPrompt) {
        AlertDialog(
            onDismissRequest = { showAccountPrompt = false },
            title = {
                Text("Save your style", fontWeight = FontWeight.Bold, color = Charcoal)
            },
            text = {
                Text("Create an account to keep your looks and track products.", color = SoftCharcoal)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccountPrompt = false
                        navController.navigate(Screen.Onboarding.route) { popUpTo(0) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Lavender, contentColor = Color.White)
                ) {
                    Text("Create Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountPrompt = false }) {
                    Text("Continue as Guest", color = Charcoal)
                }
            },
            containerColor = WarmIvory,
            titleContentColor = Charcoal,
            textContentColor = SoftCharcoal
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .verticalScroll(scrollState)
            .padding(top = 32.dp, bottom = 100.dp)
    ) {
        // 1. HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Me",
                style = Typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )

            if (!SessionManager.isGuest) {
                Surface(
                    onClick = { navController.navigate(Screen.TriesCredits.route) },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, WarmGray)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Credits",
                            tint = Lavender,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${SessionManager.credits} Credits",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Charcoal
                        )
                    }
                }
            }
        }

        // 2. PROFILE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (SessionManager.isGuest) {
                // Guest Profile
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(OffWhite, CircleShape)
                        .border(1.dp, WarmGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = SoftCharcoal)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Guest", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Create Account",
                        fontSize = 14.sp,
                        color = Lavender,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(0)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Create an account to save your style.", fontSize = 12.sp, color = SoftCharcoal)
                }
            } else {
                // Logged In Profile
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(OffWhite)
                        .border(1.dp, WarmGray, CircleShape)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Maya Sharma", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("maya.sharma@example.com", fontSize = 13.sp, color = SoftCharcoal)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Edit Profile",
                        fontSize = 13.sp,
                        color = Lavender,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Edit profile settings", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // 3. MY TRY-ON PHOTOS
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "MY TRY-ON PHOTOS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = SoftCharcoal,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (SessionManager.isGuest) {
                Text(
                    text = "Create an account to save photos for future try-ons.",
                    fontSize = 13.sp,
                    color = SoftCharcoal,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { navController.navigate(Screen.Onboarding.route) { popUpTo(0) } }
                        .padding(vertical = 4.dp)
                ) {
                    Text("Create Account", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Charcoal)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Charcoal,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=100&q=80",
                            "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=100&q=80",
                            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&q=80"
                        ).forEach { url ->
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OffWhite)
                                    .border(1.dp, WarmGray, RoundedCornerShape(10.dp))
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    Text("3 / 5 photos", fontSize = 13.sp, color = SoftCharcoal)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            Toast.makeText(context, "Manage your 3 photos", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Text("Manage Photos", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Charcoal)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Charcoal,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // 4. YOUR ACTIVITY
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "YOUR ACTIVITY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = SoftCharcoal,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(16.dp))
                    .border(1.dp, WarmGray, RoundedCornerShape(16.dp))
            ) {
                SimpleRow(
                    title = "Saved Looks",
                    onClick = {
                        if (SessionManager.isGuest) showAccountPrompt = true else navController.navigate(Screen.Looks.route)
                    }
                )
                HorizontalDivider(color = WarmGray, modifier = Modifier.padding(horizontal = 16.dp))
                SimpleRow(
                    title = "Price Tracking",
                    onClick = {
                        if (SessionManager.isGuest) showAccountPrompt = true else navController.navigate(Screen.Looks.route)
                    }
                )
            }
        }

        // 5. SETTINGS
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "SETTINGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = SoftCharcoal,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(16.dp))
                    .border(1.dp, WarmGray, RoundedCornerShape(16.dp))
            ) {
                SimpleRow(
                    title = "Notifications",
                    onClick = {
                        Toast.makeText(context, "Notification preferences", Toast.LENGTH_SHORT).show()
                    }
                )
                HorizontalDivider(color = WarmGray, modifier = Modifier.padding(horizontal = 16.dp))

                // Price Drop Switch Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Price Drop Notifications", fontSize = 15.sp, color = Charcoal, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = priceDropEnabled,
                        onCheckedChange = { priceDropEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = White,
                            checkedTrackColor = Lavender,
                            uncheckedThumbColor = White,
                            uncheckedTrackColor = WarmGray,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }

                HorizontalDivider(color = WarmGray, modifier = Modifier.padding(horizontal = 16.dp))
                SimpleRow(
                    title = "Privacy & Data",
                    onClick = {
                        Toast.makeText(context, "Privacy & Data settings", Toast.LENGTH_SHORT).show()
                    }
                )
                HorizontalDivider(color = WarmGray, modifier = Modifier.padding(horizontal = 16.dp))
                SimpleRow(
                    title = "Help & Support",
                    onClick = {
                        Toast.makeText(context, "Help & FAQ", Toast.LENGTH_SHORT).show()
                    }
                )
                HorizontalDivider(color = WarmGray, modifier = Modifier.padding(horizontal = 16.dp))
                SimpleRow(
                    title = "About OnMe",
                    onClick = {
                        Toast.makeText(context, "OnMe AI Outfit Try-On v1.0", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // 6. ACCOUNT ACTIONS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!SessionManager.isGuest) {
                Text(
                    text = "Log Out",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal,
                    modifier = Modifier
                        .clickable {
                            SessionManager.isGuest = true
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(0)
                            }
                        }
                        .padding(vertical = 12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Delete Account",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE53935),
                    modifier = Modifier
                        .clickable {
                            Toast.makeText(context, "Account deletion requested", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SimpleRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 15.sp, color = Charcoal, fontWeight = FontWeight.Medium)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = SoftCharcoal,
            modifier = Modifier.size(16.dp)
        )
    }
}
