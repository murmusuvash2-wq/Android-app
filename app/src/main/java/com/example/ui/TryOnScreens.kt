package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import com.example.ui.motion.TiHinAnimatedHeartIcon
import com.example.ui.motion.tihinButtonPress
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DeepForest
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PurchaseCTA
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftCharcoal
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.SurfaceVariantColor
import com.example.ui.theme.WarmIvory
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TryOnScreen(navController: NavController) {
    val context = LocalContext.current
    var showPhotoPickerSheet by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                TryOnManager.addTryOnPhoto(uri.toString())
            }
        }
    )

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                TryOnManager.addTryOnPhoto(tempCameraUri.toString())
            }
            tempCameraUri = null
        }
    )

    val productName = TryOnManager.selectedProductName ?: "Selected Garment"
    val productBrand = TryOnManager.selectedProductBrand ?: "Brand"
    val productPrice = TryOnManager.selectedProductPrice
    val productImage = TryOnManager.selectedProductImage
    val userPhotoUri = TryOnManager.selectedUserPhotoUri
    val credits = SessionManager.credits

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
            .testTag("try_on_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
            }
            Text(
                text = "Universal Try-On",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }

        // Side-by-Side or Stack Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Garment Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(230.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(SurfaceVariantColor)
                    ) {
                        if (productImage != null) {
                            AsyncImage(
                                model = productImage,
                                contentDescription = productName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = productBrand.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryText
                        )
                        Text(
                            text = productName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "₹${productPrice.toInt()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }
                }
            }

            // User Photo Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(230.dp)
                    .clickable { showPhotoPickerSheet = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, if (userPhotoUri == null) ChampagneGold else BorderColor)
            ) {
                if (userPhotoUri != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = userPhotoUri,
                            contentDescription = "Your Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(PrimaryText.copy(alpha = 0.65f))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Change Photo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SurfaceColor
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariantColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = DeepForest,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (userPhotoUri == null) "Choose your photo" else "Change photo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Text(
                            text = if (TryOnManager.tryOnPhotos.isEmpty()) "Choose from your saved Try-On photos or add a new one" else "${TryOnManager.tryOnPhotos.size}/5 saved photos",
                            fontSize = 11.sp,
                            color = SecondaryText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Credit Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Virtual Fitting Cost",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                    Text(
                        text = "1 credit held during generation",
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariantColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$credits Credits left",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Confirm Button
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            val confirmInteraction = remember { MutableInteractionSource() }
            Button(
                onClick = {
                    if (SessionManager.holdCredit()) {
                        navController.navigate(Screen.Processing.route)
                    } else {
                        Toast.makeText(context, "Insufficient credits. Please add credits.", Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.CreditStore.route)
                    }
                },
                enabled = userPhotoUri != null,
                interactionSource = confirmInteraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("try_on_confirm_button")
                    .tihinButtonPress(confirmInteraction),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepForest,
                    contentColor = SurfaceColor
                )
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (userPhotoUri == null) "Add photo above to try on" else "Confirm & Generate Try-On",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showPhotoPickerSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPhotoPickerSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Choose your photo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Text(
                    text = "Select one of your saved Try-On photos.",
                    fontSize = 12.sp,
                    color = SecondaryText
                )

                if (TryOnManager.tryOnPhotos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TryOnManager.tryOnPhotos.forEach { photo ->
                            val selected = TryOnManager.selectedUserPhotoUri == photo.uri
                            Column(
                                modifier = Modifier
                                    .width(76.dp)
                                    .clickable {
                                        TryOnManager.selectTryOnPhoto(photo.uri)
                                        showPhotoPickerSheet = false
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceVariantColor)
                                ) {
                                    AsyncImage(
                                        model = photo.uri,
                                        contentDescription = photo.label,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (selected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(5.dp)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(DeepForest),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = SurfaceColor, modifier = Modifier.size(13.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(photo.label, fontSize = 10.sp, color = PrimaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (TryOnManager.canAddTryOnPhoto()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoPickerSheet = false
                                val uri = TryOnManager.createTempCameraUri(context)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            }
                            .padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = DeepForest)
                        Spacer(modifier = Modifier.size(14.dp))
                        Column {
                            Text("Take a new photo", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                            Text("${TryOnManager.getTryOnPhotoCount()}/5 saved", fontSize = 11.sp, color = SecondaryText)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoPickerSheet = false
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = DeepForest)
                        Spacer(modifier = Modifier.size(14.dp))
                        Text("Choose from Gallery", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                    }
                } else {
                    Text(
                        text = "5 Try-On photos saved. Manage them from Profile.",
                        fontSize = 12.sp,
                        color = SecondaryText,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProcessingScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(0.95f) }
    val tips = listOf(
        "Uploading photo to secure storage...",
        "Preserving fabric texture, drape, and seam cut...",
        "Aligning outfit contours to your pose...",
        "Balancing ambient lighting and shadows...",
        "Refining neckline, sleeves, and garment silhouette..."
    )
    var tipIndex by remember { mutableIntStateOf(0) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentStep by remember { mutableStateOf("Connecting to AI Try-On service...") }
    val activeRequestId = remember { TryOnManager.lastRequestId ?: "req_${java.util.UUID.randomUUID()}" }

    fun cancelTryOn() {
        TryOnManager.isProcessing = false
        SessionManager.releaseHeldCredit()
        scope.launch(Dispatchers.IO) {
            try {
                val sessionResult = com.example.data.datasource.SupabaseAuthManager.ensureAuthenticatedSession()
                if (sessionResult.isSuccess) {
                    val (_, token) = sessionResult.getOrThrow()
                    val config = com.example.data.datasource.SupabaseConfig
                    config.functionsApi.generateTryOn(
                        apiKey = config.anonKey,
                        authHeader = "Bearer $token",
                        request = com.example.data.datasource.GenerateTryOnRequestDto(
                            action = "cancel",
                            requestId = activeRequestId
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        navController.popBackStack()
    }

    BackHandler(enabled = !isError) {
        cancelTryOn()
    }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        while (!isError) {
            delay(2000)
            tipIndex = (tipIndex + 1) % tips.size
        }
    }

    LaunchedEffect(Unit) {
        val userPhotoUri = TryOnManager.selectedUserPhotoUri
        val productId = TryOnManager.selectedProductId ?: "1"
        val requestId = activeRequestId
        TryOnManager.lastRequestId = requestId
        TryOnManager.isProcessing = true
        TryOnManager.lastError = null

        if (userPhotoUri == null) {
            isError = true
            errorMessage = "No photo selected. Please choose a photo first."
            TryOnManager.isProcessing = false
            SessionManager.releaseHeldCredit()
            return@LaunchedEffect
        }

        currentStep = "Uploading photo to tryon-photos storage..."
        val tryOnService = com.example.data.datasource.TryOnServiceProvider.get()
        val result = tryOnService.executeTryOn(
            context = context,
            productId = productId,
            userPhotoUri = userPhotoUri,
            requestId = requestId
        )

        when (result) {
            is com.example.data.datasource.TryOnResult.Success -> {
                TryOnManager.isProcessing = false
                SessionManager.consumeHeldCredit()

                val resultUri = result.signedResultUrl
                if (resultUri.isNullOrBlank()) {
                    isError = true
                    errorMessage = "Virtual try-on result could not be retrieved."
                    TryOnManager.lastError = errorMessage
                    return@LaunchedEffect
                }
                TryOnManager.generatedResultImageUri = resultUri

                val productName = TryOnManager.selectedProductName ?: "Selected Garment"
                val productBrand = TryOnManager.selectedProductBrand ?: "Brand"
                val productPrice = TryOnManager.selectedProductPrice

                TiHinStyleRepository.addSavedLook(
                    SavedLook(
                        id = "look_${System.currentTimeMillis()}",
                        productId = productId,
                        productName = productName,
                        brand = productBrand,
                        price = productPrice,
                        resultImageUri = resultUri
                    )
                )

                navController.navigate(Screen.Result.route) {
                    popUpTo(Screen.Processing.route) { inclusive = true }
                }
            }
            is com.example.data.datasource.TryOnResult.Failure -> {
                TryOnManager.isProcessing = false
                SessionManager.releaseHeldCredit()
                TryOnManager.lastError = result.errorMessage
                isError = true
                errorMessage = result.errorMessage
            }
            is com.example.data.datasource.TryOnResult.Error -> {
                TryOnManager.isProcessing = false
                SessionManager.releaseHeldCredit()
                val msg = result.exception.message ?: "An unexpected error occurred"
                TryOnManager.lastError = msg
                isError = true
                errorMessage = msg
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("processing_screen"),
        contentAlignment = Alignment.Center
    ) {
        if (!isError) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(scale.value)
                        .clip(CircleShape)
                        .background(DeepForest.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = DeepForest,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Fitting Your Look",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedContent(
                    targetState = tips[tipIndex],
                    transitionSpec = {
                        fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) togetherWith
                                fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing))
                    },
                    label = "processing_tip_fade"
                ) { tipText ->
                    Text(
                        text = tipText,
                        fontSize = 14.sp,
                        color = SecondaryText,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                CircularProgressIndicator(
                    color = DeepForest,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing)) togetherWith
                                fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                    },
                    label = "processing_step_fade"
                ) { stepText ->
                    Text(
                        text = stepText,
                        fontSize = 12.sp,
                        color = SecondaryText.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                val cancelInteraction = remember { MutableInteractionSource() }
                OutlinedButton(
                    onClick = { cancelTryOn() },
                    interactionSource = cancelInteraction,
                    modifier = Modifier
                        .testTag("cancel_tryon_button")
                        .tihinButtonPress(cancelInteraction),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SecondaryText.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryText)
                ) {
                    Text("Cancel", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Try-On Request Failed",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = errorMessage ?: "Could not complete virtual try-on request.",
                    fontSize = 14.sp,
                    color = SecondaryText,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your credit has been safely returned.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = DeepForest,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepForest,
                        contentColor = SurfaceColor
                    )
                ) {
                    Text("Return to Try-On", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ResultScreen(navController: NavController) {
    val context = LocalContext.current
    val productId = TryOnManager.selectedProductId ?: "product"
    val productName = TryOnManager.selectedProductName ?: "Garment"
    val productBrand = TryOnManager.selectedProductBrand ?: "Brand"
    val productPrice = TryOnManager.selectedProductPrice
    val resultImage = TryOnManager.generatedResultImageUri
        ?: TryOnManager.selectedProductImage
        ?: "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800"

    val isFavourite = TiHinStyleRepository.isFavourite(productId)
    val isPriceTracked = TiHinStyleRepository.isPriceTracked(productId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
            .testTag("result_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home", tint = PrimaryText)
            }
            Text(
                text = "Your New Look",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            val saveInteraction = remember { MutableInteractionSource() }
            IconButton(
                onClick = {
                    TiHinStyleRepository.toggleFavourite(
                        productId = productId,
                        productName = productName,
                        merchant = productBrand,
                        price = productPrice,
                        imageUrl = resultImage
                    )
                },
                interactionSource = saveInteraction,
                modifier = Modifier.tihinButtonPress(saveInteraction, pressedScale = 0.92f)
            ) {
                TiHinAnimatedHeartIcon(
                    isFavourite = isFavourite,
                    contentDescription = "Save",
                    activeTint = DeepForest,
                    inactiveTint = SecondaryText,
                    iconSize = 22.dp
                )
            }
        }

        // Hero Look Image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .height(380.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SubcomposeAsyncImage(
                    model = resultImage,
                    contentDescription = "Fitted Look",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SurfaceColor),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = DeepForest,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SurfaceVariantColor)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Unable to load image",
                                    tint = SecondaryText,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Unable to load result image.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryText,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "The secure link may have expired or is invalid.",
                                    fontSize = 11.sp,
                                    color = SecondaryText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    },
                    success = {
                        SubcomposeAsyncImageContent()
                    }
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceColor.copy(alpha = 0.88f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "TiHin AI Fitted",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepForest
                    )
                }
            }
        }

        // Garment Info
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            Text(
                text = productBrand.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryText,
                letterSpacing = 0.5.sp
            )
            Text(
                text = productName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${productPrice.toInt()}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }

        // Actions Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val shareInteraction = remember { MutableInteractionSource() }
            OutlinedButton(
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, "Check out how this $productName from $productBrand looks on me via TiHin!")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Look"))
                },
                interactionSource = shareInteraction,
                modifier = Modifier
                    .weight(1f)
                    .tihinButtonPress(shareInteraction),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("Share", fontSize = 13.sp)
            }

            val priceInteraction = remember { MutableInteractionSource() }
            OutlinedButton(
                onClick = {
                    TiHinStyleRepository.togglePriceTracking(productId)
                    Toast.makeText(
                        context,
                        if (isPriceTracked) "Price tracking removed" else "Price drop alert active",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                interactionSource = priceInteraction,
                modifier = Modifier
                    .weight(1f)
                    .tihinButtonPress(priceInteraction),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isPriceTracked) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = if (isPriceTracked) ChampagneGold else SecondaryText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(if (isPriceTracked) "Tracked ✓" else "Price Drop", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Purchase CTA Button
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            val buyInteraction = remember { MutableInteractionSource() }
            Button(
                onClick = {
                    Toast.makeText(context, "Opening merchant store...", Toast.LENGTH_SHORT).show()
                },
                interactionSource = buyInteraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("result_buy_button")
                    .tihinButtonPress(buyInteraction),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurchaseCTA,
                    contentColor = SurfaceColor
                )
            ) {
                Text(
                    text = "Buy on $productBrand ↗",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SharedProductErrorScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("shared_error_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = DeepForest,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Product Not Found",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We couldn't automatically load the garment from the shared link. Browse trending styles in Discover to find similar outfits.",
                fontSize = 14.sp,
                color = SecondaryText,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate(Screen.Discover.route) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurchaseCTA,
                    contentColor = SurfaceColor
                )
            ) {
                Text("Browse Discover", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
