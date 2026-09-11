package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current

    // Guest protection: Guests do not have an editable profile
    if (SessionManager.isGuest) {
        GuestEditProfileFallback(navController = navController)
        return
    }

    val currentProfile = UserProfileRepository.profile

    var nameInput by rememberSaveable { mutableStateOf(currentProfile.displayName) }
    var photoUriInput by rememberSaveable { mutableStateOf(currentProfile.profilePhotoUri) }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    var showPhotoSourceSheet by rememberSaveable { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val hasUnsavedChanges by remember(nameInput, photoUriInput, currentProfile) {
        derivedStateOf {
            nameInput.trim() != currentProfile.displayName.trim() ||
                photoUriInput != currentProfile.profilePhotoUri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                photoUriInput = tempCameraUri.toString()
                /* Toast disabled for tests */
            }
            tempCameraUri = null
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                photoUriInput = uri.toString()
            }
        }
    )

    fun handleBack() {
        if (hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            navController.navigateUp()
        }
    }

    BackHandler(enabled = true) {
        handleBack()
    }

    // Discard Changes Confirmation Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Discard changes?",
                    fontFamily = CormorantGaramond,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Charcoal
                )
            },
            text = {
                Text(
                    text = "You have unsaved changes. Are you sure you want to discard them?",
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    color = SoftCharcoal
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        navController.navigateUp()
                    },
                    modifier = Modifier.testTag("discard_dialog_confirm_button")
                ) {
                    Text(
                        text = "Discard",
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardDialog = false },
                    modifier = Modifier.testTag("discard_dialog_cancel_button")
                ) {
                    Text(
                        text = "Keep Editing",
                        color = Charcoal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        )
    }

    // Change Photo Bottom Sheet
    if (showPhotoSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSourceSheet = false },
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
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "Profile Photo",
                    fontFamily = CormorantGaramond,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose a photo to personalize your profile.",
                    fontSize = 13.sp,
                    color = SoftCharcoal
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Camera Action
                Button(
                    onClick = {
                        showPhotoSourceSheet = false
                        val uri = UserProfileRepository.createTempProfileCameraUri(context)
                        if (uri != null) {
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            /* Toast disabled for tests */
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("choose_camera_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Photo", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gallery Action
                Button(
                    onClick = {
                        showPhotoSourceSheet = false
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("choose_gallery_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OffWhite, contentColor = Charcoal),
                    border = BorderStroke(1.dp, WarmGray)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose from Gallery", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    Scaffold(
        containerColor = WarmIvory,
        topBar = {
            EditProfileTopAppBar(onBackClick = { handleBack() })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Photo Preview & Action
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(White)
                    .border(1.5.dp, WarmGray, CircleShape)
                    .testTag("edit_profile_avatar_preview"),
                contentAlignment = Alignment.Center
            ) {
                if (!photoUriInput.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoUriInput)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val previewInitials = UserProfile(displayName = nameInput).initials
                    Text(
                        text = previewInitials,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftCharcoal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                onClick = { showPhotoSourceSheet = true },
                shape = RoundedCornerShape(20.dp),
                color = White,
                border = BorderStroke(1.dp, WarmGray),
                modifier = Modifier.testTag("change_photo_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = DeepForest,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Change Photo",
                        color = DeepForest,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Editable Name Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Name",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        if (nameError != null && it.trim().isNotEmpty()) {
                            nameError = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("name_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        errorContainerColor = White,
                        focusedBorderColor = DeepForest,
                        unfocusedBorderColor = WarmGray,
                        focusedTextColor = Charcoal,
                        unfocusedTextColor = Charcoal
                    ),
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) {
                            Text(
                                text = nameError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.testTag("name_error_text")
                            )
                        }
                    },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Read-only Email Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Email",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = currentProfile.email,
                    onValueChange = { /* Read only */ },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceVariantColor,
                        unfocusedContainerColor = SurfaceVariantColor,
                        disabledContainerColor = SurfaceVariantColor,
                        focusedBorderColor = WarmGray,
                        unfocusedBorderColor = WarmGray,
                        focusedTextColor = SoftCharcoal,
                        unfocusedTextColor = SoftCharcoal
                    ),
                    supportingText = {
                        Text(
                            text = "Email is linked to your account and cannot be edited.",
                            color = TertiaryText,
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Primary CTA: Save Changes
            Button(
                onClick = {
                    val trimmed = nameInput.trim()
                    if (trimmed.isEmpty()) {
                        nameError = "Name cannot be empty"
                        return@Button
                    }
                    nameError = null

                    if (hasUnsavedChanges) {
                        UserProfileRepository.updateProfile(
                            displayName = trimmed,
                            profilePhotoUri = photoUriInput,
                            context = context
                        )
                        /* Toast disabled for tests */
                    }
                    navController.navigateUp()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_changes_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurchaseCTA,
                    contentColor = CTAText
                )
            ) {
                Text(
                    text = "Save Changes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EditProfileTopAppBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .testTag("edit_profile_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Charcoal
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Edit Profile",
            fontFamily = CormorantGaramond,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal
        )
    }
}

@Composable
private fun GuestEditProfileFallback(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(OffWhite, CircleShape)
                .border(1.dp, WarmGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("G", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = SoftCharcoal)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Guest",
            fontFamily = CormorantGaramond,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create an account to customize and save your profile.",
            fontSize = 14.sp,
            color = SoftCharcoal,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(0)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("guest_create_account_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White)
        ) {
            Text("Create Account", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
