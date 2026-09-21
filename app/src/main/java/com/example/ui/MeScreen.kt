package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.motion.tihinButtonPress
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.Charcoal
import com.example.ui.theme.DeepForest
import com.example.ui.theme.DeepForestContainer
import com.example.ui.theme.EditorialSerif
import com.example.ui.theme.Inter
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftCharcoal
import com.example.ui.theme.SubtleCardElevation
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.SurfaceVariantColor
import com.example.ui.theme.WarmIvory

@Composable
fun MeScreen(
    navController: NavController,
    onNavigateToLooksTab: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val isGuest = SessionManager.isGuest
    val userEmail = SessionManager.userEmail
    val credits = SessionManager.credits

    val userDisplayName = SessionManager.userDisplayName ?: (if (isGuest) "Guest User" else (userEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "TiHin Member"))
    var pushNotificationsEnabled by remember { mutableStateOf(SessionManager.notificationsEnabled) }
    var priceDropNotificationsEnabled by remember { mutableStateOf(SessionManager.priceDropNotificationsEnabled) }

    // Dialog & Sheet States
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showManagePhotosDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    // Camera & Gallery launchers for Manage Photos
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                TryOnManager.updateUserPhoto(tempCameraUri.toString(), context)
            }
            tempCameraUri = null
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flag)
                } catch (e: Exception) {
                    // Fallback for cases where persistence is not allowed or already granted
                }
                TryOnManager.updateUserPhoto(uri.toString(), context)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 36.dp)
            .testTag("me_screen")
    ) {
        // Top Header
        Text(
            text = "Profile",
            fontFamily = EditorialSerif,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        // ==========================================
        // 1. PROFILE SECTION
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            border = BorderStroke(1.dp, CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = SubtleCardElevation)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(DeepForestContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = DeepForest,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = if (isGuest) "Guest User" else userDisplayName,
                            fontFamily = Inter,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isGuest) "Sign in to save try-ons & sync" else (userEmail ?: "Member"),
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = SecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!isGuest) {
                        val creditInteraction = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(ChampagneGold.copy(alpha = 0.15f))
                                .tihinButtonPress(creditInteraction, pressedScale = 0.94f)
                                .clickable(
                                    interactionSource = creditInteraction,
                                    indication = null
                                ) { navController.navigate(Screen.CreditStore.route) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$credits Credits",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                navController.navigate(Screen.Onboarding.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DeepForest),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepForest),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Sign In", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Inter)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Edit Profile Entry Point
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showEditProfileDialog = true }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = DeepForest,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Edit Profile",
                            fontFamily = Inter,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepForest
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = SecondaryText,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 2. YOUR ACTIVITY SECTION
        // ==========================================
        Text(
            text = "YOUR ACTIVITY",
            fontFamily = Inter,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = SoftCharcoal,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            border = BorderStroke(1.dp, CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = SubtleCardElevation)
        ) {
            Column {
                val hasPhoto = TryOnManager.selectedUserPhotoUri != null
                ProfileOptionRow(
                    icon = Icons.Default.CameraAlt,
                    title = "My Try-On Photos",
                    subtitle = if (hasPhoto) "1 active photo · Tap to manage" else "No photo uploaded · Tap to add",
                    onClick = { showManagePhotosDialog = true }
                )
                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                ProfileOptionRow(
                    icon = Icons.Default.FavoriteBorder,
                    title = "Saved Looks",
                    subtitle = "${TiHinStyleRepository.favouriteProductIds.size} saved styles",
                    onClick = { onNavigateToLooksTab(1) }
                )
                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                ProfileOptionRow(
                    icon = Icons.Default.NotificationsNone,
                    title = "Price Tracking",
                    subtitle = "${TiHinStyleRepository.priceTrackedProductIds.size} active price watches",
                    onClick = { onNavigateToLooksTab(2) }
                )
                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                ProfileOptionRow(
                    icon = Icons.Default.CardGiftcard,
                    title = "Get More Credits",
                    subtitle = "Purchase virtual fitting packages",
                    onClick = { navController.navigate(Screen.CreditStore.route) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 3. SETTINGS SECTION
        // ==========================================
        Text(
            text = "SETTINGS & PREFERENCES",
            fontFamily = Inter,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = SoftCharcoal,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            border = BorderStroke(1.dp, CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = SubtleCardElevation)
        ) {
            Column {
                // Push Notifications Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notifications",
                            fontFamily = Inter,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Charcoal
                        )
                        Text(
                            text = "Receive alerts for new collections and style drops",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }
                    Switch(
                        checked = pushNotificationsEnabled,
                        onCheckedChange = { 
                            pushNotificationsEnabled = it
                            SessionManager.setNotificationsEnabled(it, context)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceColor,
                            checkedTrackColor = DeepForest
                        )
                    )
                }

                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                // Price Drop Notifications Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Price Drop Notifications",
                            fontFamily = Inter,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Charcoal
                        )
                        Text(
                            text = "Get notified when tracked garments go on sale",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }
                    Switch(
                        checked = priceDropNotificationsEnabled,
                        onCheckedChange = {
                            priceDropNotificationsEnabled = it
                            SessionManager.setPriceDropNotifications(it, context)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceColor,
                            checkedTrackColor = DeepForest
                        )
                    )
                }

                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                // Privacy & Data
                ProfileOptionRow(
                    icon = Icons.Default.Security,
                    title = "Privacy & Data",
                    subtitle = "Manage photo processing and privacy controls",
                    onClick = { showPrivacyDialog = true }
                )

                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                // Help & Support
                ProfileOptionRow(
                    icon = Icons.Default.HelpOutline,
                    title = "Help & Support",
                    subtitle = "Concierge, styling guide, and FAQs",
                    onClick = { showHelpDialog = true }
                )

                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                // About TiHin
                ProfileOptionRow(
                    icon = Icons.Default.Info,
                    title = "About TiHin",
                    subtitle = "Version 1.0.0 · Try. Love. Buy.",
                    onClick = { showAboutDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 4. ACCOUNT SECTION
        // ==========================================
        Text(
            text = "ACCOUNT",
            fontFamily = Inter,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = SoftCharcoal,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Log Out Button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_logout_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceColor,
                    contentColor = Charcoal
                ),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Charcoal
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (isGuest) "Exit Guest Mode" else "Log Out",
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Delete Account Button
            OutlinedButton(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_delete_account_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB3261E)),
                border = BorderStroke(1.dp, Color(0xFFB3261E).copy(alpha = 0.35f))
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFFB3261E)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Delete Account",
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // ==========================================
    // DIALOGS & ACTION SHEETS
    // ==========================================

    // 1. Edit Profile Dialog
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userDisplayName) }
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = SurfaceColor,
            title = {
                Text(
                    text = "Edit Profile",
                    fontFamily = EditorialSerif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Customize how your name appears in TiHin.",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepForest,
                            unfocusedBorderColor = CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isGuest && userEmail != null) {
                        Text(
                            text = "Account: $userEmail",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            SessionManager.updateDisplayName(tempName.trim(), context)
                        }
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepForest)
                ) {
                    Text("Save Changes", fontFamily = Inter, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = SecondaryText, fontFamily = Inter)
                }
            }
        )
    }

    // 2. Manage Photos Dialog
    if (showManagePhotosDialog) {
        val currentPhoto = TryOnManager.selectedUserPhotoUri
        AlertDialog(
            onDismissRequest = { showManagePhotosDialog = false },
            containerColor = SurfaceColor,
            title = {
                Text(
                    text = "My Try-On Photos",
                    fontFamily = EditorialSerif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (currentPhoto != null) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceVariantColor)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(currentPhoto)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Active Try-On Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = "This photo is used as your model for fitting garments.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariantColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = DeepForest,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Text(
                            text = "Upload a full-body photo to preview any outfit instantly on yourself.",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = SecondaryText
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val uri = TryOnManager.createTempCameraUri(context)
                                if (uri != null) {
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                }
                                showManagePhotosDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                showManagePhotosDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepForest),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", fontSize = 12.sp)
                        }
                    }

                    if (currentPhoto != null) {
                        OutlinedButton(
                            onClick = {
                                TryOnManager.selectedUserPhotoUri = null
                                showManagePhotosDialog = false
                            },
                            border = BorderStroke(1.dp, Color(0xFFB3261E).copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB3261E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Remove Photo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManagePhotosDialog = false }) {
                    Text("Close", color = Charcoal, fontFamily = Inter)
                }
            }
        )
    }

    // 3. Privacy & Data Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = SurfaceColor,
            title = {
                Text(
                    text = "Privacy & Data Protection",
                    fontFamily = EditorialSerif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TiHin safeguards your personal style and fitting images:",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Charcoal
                    )
                    Text(
                        text = "• Try-On photos are strictly used for your personal on-demand virtual fitting requests.",
                        fontFamily = Inter,
                        fontSize = 12.5.sp,
                        color = SecondaryText
                    )
                    Text(
                        text = "• We never train public biometric models on your images.",
                        fontFamily = Inter,
                        fontSize = 12.5.sp,
                        color = SecondaryText
                    )
                    Text(
                        text = "• You retain 100% ownership of your photos and can delete them at any time from My Try-On Photos.",
                        fontFamily = Inter,
                        fontSize = 12.5.sp,
                        color = SecondaryText
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepForest)
                ) {
                    Text("Understood", fontFamily = Inter)
                }
            }
        )
    }

    // 4. Help & Support Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = SurfaceColor,
            title = {
                Text(
                    text = "TiHin Concierge",
                    fontFamily = EditorialSerif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "We are here to elevate your virtual fitting journey.",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                    Text(
                        text = "• Credits: 1 successful Try-On = 1 credit. Free credits replenish or you can purchase credit packs in Get More Credits.",
                        fontFamily = Inter,
                        fontSize = 12.5.sp,
                        color = Charcoal
                    )
                    Text(
                        text = "• Fitting Tips: Use a front-facing, well-lit photo with arms slightly away from your sides for optimal garment silhouette accuracy.",
                        fontFamily = Inter,
                        fontSize = 12.5.sp,
                        color = Charcoal
                    )
                    Text(
                        text = "• Concierge Support: support@tihin.fashion",
                        fontFamily = Inter,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepForest
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepForest)
                ) {
                    Text("Close", fontFamily = Inter)
                }
            }
        )
    }

    // 5. About TiHin Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = SurfaceColor,
            title = {
                Text(
                    text = "TiHin",
                    fontFamily = EditorialSerif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "“Try. Love. Buy.”",
                        fontFamily = EditorialSerif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = DeepForest
                    )
                    Text(
                        text = "Version 1.0.0 (Production Build)",
                        fontFamily = Inter,
                        fontSize = 12.5.sp,
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TiHin bridges the gap between digital discovery and real-world confidence, bringing luxury virtual fittings directly to your device with photorealistic drape, texture, and silhouette fidelity.",
                        fontFamily = Inter,
                        fontSize = 12.5.sp,
                        color = Charcoal
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepForest)
                ) {
                    Text("Close", fontFamily = Inter)
                }
            }
        )
    }

    // 6. Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = SurfaceColor,
            title = {
                Text(
                    text = if (isGuest) "Exit Guest Mode?" else "Log Out?",
                    fontFamily = EditorialSerif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            },
            text = {
                Text(
                    text = if (isGuest) "You will be returned to the welcome screen to sign in or register." else "Are you sure you want to log out of your TiHin account?",
                    fontFamily = Inter,
                    fontSize = 13.5.sp,
                    color = SecondaryText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        SessionManager.logout(context)
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal)
                ) {
                    Text(if (isGuest) "Exit" else "Log Out", fontFamily = Inter, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = SecondaryText, fontFamily = Inter)
                }
            }
        )
    }

    // 7. Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = SurfaceColor,
            title = {
                Text(
                    text = "Delete Account?",
                    fontFamily = EditorialSerif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB3261E)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Are you sure you want to permanently delete your TiHin account?",
                        fontFamily = Inter,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Charcoal
                    )
                    Text(
                        text = "This action will immediately remove all your saved looks, active price watches, fitting photos, and remaining credits from this device.\n\nNote: Real server-side account deletion must be requested via support@tihin.fashion.",
                        fontFamily = Inter,
                        fontSize = 12.5.sp,
                        color = SecondaryText
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        // Clean local user state
                        TryOnManager.selectedUserPhotoUri = null
                        TiHinStyleRepository.resetForTesting(context)
                        SessionManager.logout(context)
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
                ) {
                    Text("Delete Account", fontFamily = Inter, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = SecondaryText, fontFamily = Inter)
                }
            }
        )
    }
}

@Composable
fun ProfileOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .tihinButtonPress(interactionSource, pressedScale = 0.98f)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DeepForestContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DeepForest,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = title,
                fontFamily = Inter,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal
            )
            Text(
                text = subtitle,
                fontFamily = Inter,
                fontSize = 12.sp,
                color = SecondaryText
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = SecondaryText,
            modifier = Modifier.size(14.dp)
        )
    }
}
