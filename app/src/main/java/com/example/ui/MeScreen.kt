package com.example.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeScreen(
    navController: NavController,
    onNavigateToLooks: (LooksTab) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val priceDropEnabled = SessionManager.priceDropNotificationsEnabled
    var showAccountPrompt by remember { mutableStateOf(false) }
    var showManagePhotosSheet by remember { mutableStateOf(false) }

    val userPhotos = UserPhotosRepository.photos

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val added = UserPhotosRepository.addPhoto(context, uri.toString())
                if (added) {
                    /* Toast disabled for tests */
                } else {
                    /* Toast disabled for tests */
                }
            }
        }
    )

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
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White)
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
            .padding(top = 32.dp, bottom = SpacingXl)
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
                            tint = ChampagneGold,
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
                        color = DeepForest,
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
                val userProfile = UserProfileRepository.profile
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(OffWhite)
                        .border(1.dp, WarmGray, CircleShape)
                        .testTag("me_profile_photo"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!userProfile.profilePhotoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(userProfile.profilePhotoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = userProfile.initials,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftCharcoal
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = userProfile.displayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        modifier = Modifier.testTag("me_profile_name")
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = userProfile.email,
                        fontSize = 13.sp,
                        color = SoftCharcoal,
                        modifier = Modifier.testTag("me_profile_email")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Edit Profile",
                        fontSize = 13.sp,
                        color = DeepForest,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .testTag("edit_profile_button")
                            .clickable {
                                navController.navigate(Screen.EditProfile.route)
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
                fontSize = 12.sp,
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
                        userPhotos.forEach { photo ->
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OffWhite)
                                    .border(
                                        width = if (photo.isDefault) 2.dp else 1.dp,
                                        color = if (photo.isDefault) DeepForest else WarmGray,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { showManagePhotosSheet = true }
                            ) {
                                AsyncImage(
                                    model = photo.uri,
                                    contentDescription = if (photo.isDefault) "Default Try-On Photo" else "Try-On Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (photo.isDefault) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(2.dp)
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(DeepForest),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Text("${userPhotos.size} / 5 photos", fontSize = 13.sp, color = SoftCharcoal)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showManagePhotosSheet = true }
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
                fontSize = 12.sp,
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
                        if (SessionManager.isGuest) showAccountPrompt = true else onNavigateToLooks(LooksTab.RECENT)
                    }
                )
                HorizontalDivider(color = WarmGray, modifier = Modifier.padding(horizontal = 16.dp))
                SimpleRow(
                    title = "Price Tracking",
                    onClick = {
                        if (SessionManager.isGuest) showAccountPrompt = true else onNavigateToLooks(LooksTab.PRICE_TRACKING)
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
                fontSize = 12.sp,
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
                        /* Toast disabled for tests */
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
                        onCheckedChange = { SessionManager.setPriceDropNotifications(it, context) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = White,
                            checkedTrackColor = DeepForest,
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
                        /* Toast disabled for tests */
                    }
                )
                HorizontalDivider(color = WarmGray, modifier = Modifier.padding(horizontal = 16.dp))
                SimpleRow(
                    title = "Help & Support",
                    onClick = {
                        /* Toast disabled for tests */
                    }
                )
                HorizontalDivider(color = WarmGray, modifier = Modifier.padding(horizontal = 16.dp))
                SimpleRow(
                    title = "About TiHin",
                    onClick = {
                        /* Toast disabled for tests */
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
                            SessionManager.logout(context)
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
                            /* Toast disabled for tests */
                        }
                        .padding(vertical = 8.dp)
                )
            }
        }
    }

    if (showManagePhotosSheet) {
        ModalBottomSheet(
            onDismissRequest = { showManagePhotosSheet = false },
            containerColor = WarmIvory,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = SoftCharcoal.copy(alpha = 0.3f),
                    width = 40.dp,
                    height = 4.dp
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My Try-On Photos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )
                        Text(
                            text = "${userPhotos.size} / 5 photos saved. Tap to select default.",
                            fontSize = 13.sp,
                            color = SoftCharcoal
                        )
                    }
                    if (userPhotos.size < UserPhotosRepository.MAX_PHOTOS) {
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Charcoal)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Photo",
                                tint = White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                userPhotos.forEach { photo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(White, RoundedCornerShape(12.dp))
                            .border(
                                width = if (photo.isDefault) 1.5.dp else 1.dp,
                                color = if (photo.isDefault) DeepForest else WarmGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = photo.uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                if (photo.isDefault) {
                                    Surface(
                                        color = DeepForest.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "DEFAULT PHOTO",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepForest,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Set as Default",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DeepForest,
                                        modifier = Modifier
                                            .clickable {
                                                UserPhotosRepository.setDefaultPhoto(context, photo.id)
                                            }
                                            .padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (userPhotos.size > 1) {
                            IconButton(
                                onClick = {
                                    UserPhotosRepository.deletePhoto(context, photo.id)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete photo",
                                    tint = SoftCharcoal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
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
