package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.NumberFormat
import java.util.Locale

object TryOnManager {
    var selectedProductId by mutableStateOf("prod_123")
    var selectedProductImage by mutableStateOf("https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&q=80")
    var selectedProductName by mutableStateOf("Blush Co-ord Set")
    var selectedProductBrand by mutableStateOf("URBANIC")
    var selectedProductPrice by mutableStateOf(2999.0)

    var rating by mutableStateOf<Double?>(null)
    var reviewCount by mutableStateOf<Int?>(null)
    var ratingSource by mutableStateOf<String?>(null)
    val isPriceTracked: Boolean
        get() = OnMeStyleRepository.isPriceTracked(selectedProductId)

    var selectedUserPhotoUri by mutableStateOf("")
    var generatedResultImageUri by mutableStateOf("")
    var showWatermark by mutableStateOf(true)

    fun updateUserPhoto(uri: String?) {
        if (!uri.isNullOrBlank()) {
            selectedUserPhotoUri = uri
        }
    }

    fun createTempCameraUri(context: Context): Uri? {
        return try {
            val photosDir = File(context.cacheDir, "camera_photos").apply { mkdirs() }
            val tempFile = File.createTempFile("tryon_user_", ".jpg", photosDir)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
        } catch (e: Exception) {
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TryOnScreen(navController: NavController) {
    val context = LocalContext.current
    var showAddPhotoSheet by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                TryOnManager.updateUserPhoto(tempCameraUri.toString())
                Toast.makeText(context, "Photo captured!", Toast.LENGTH_SHORT).show()
            }
            tempCameraUri = null
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                TryOnManager.updateUserPhoto(uri.toString())
            }
        }
    )

    if (showAddPhotoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddPhotoSheet = false },
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
                    text = "Add Your Photo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Choose a clear full-body or half-body portrait for best AI fit.",
                    fontSize = 13.sp,
                    color = SoftCharcoal
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        showAddPhotoSheet = false
                        val uri = TryOnManager.createTempCameraUri(context)
                        if (uri != null) {
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            Toast.makeText(context, "Unable to launch camera", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take a Photo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        showAddPhotoSheet = false
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OffWhite, contentColor = Charcoal),
                    border = BorderStroke(1.dp, WarmGray)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose from Gallery", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Charcoal)
            }
            Text("Try On", color = Charcoal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Selected Product Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(18.dp))
                    .border(1.dp, WarmGray, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(TryOnManager.selectedProductImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Selected Product",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OffWhite)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                    formatter.maximumFractionDigits = 0
                    val priceStr = formatter.format(TryOnManager.selectedProductPrice)

                    Text(
                        text = TryOnManager.selectedProductBrand.uppercase(),
                        fontSize = 12.sp,
                        color = SoftCharcoal,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = TryOnManager.selectedProductName,
                        fontSize = 15.sp,
                        color = Charcoal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = priceStr,
                        fontSize = 15.sp,
                        color = Charcoal,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("My Photo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Charcoal)
            Spacer(modifier = Modifier.height(12.dp))

            if (TryOnManager.selectedUserPhotoUri.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(OffWhite)
                        .border(1.dp, WarmGray, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(TryOnManager.selectedUserPhotoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "My Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = { showAddPhotoSheet = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Change Photo", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DeepForest)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(White, RoundedCornerShape(24.dp))
                        .border(1.dp, WarmGray, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = SoftCharcoal,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("No photo saved", color = Charcoal, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Text("Add a photo to see the outfit.", color = SoftCharcoal, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { showAddPhotoSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Add Your Photo", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val hasValidPhoto = TryOnManager.selectedUserPhotoUri.isNotBlank()
            val hasSufficientCredits = SessionManager.hasSufficientCredits
            var isSubmitting by remember { mutableStateOf(false) }

            // Ensure submission lock resets if user re-enters or returns to screen
            LaunchedEffect(Unit) {
                isSubmitting = false
            }

            val isConfirmEnabled = hasValidPhoto && hasSufficientCredits && !isSubmitting

            Button(
                onClick = {
                    if (!isConfirmEnabled) return@Button
                    isSubmitting = true
                    if (SessionManager.holdCredit()) {
                        navController.navigate(Screen.Processing.route)
                    } else {
                        isSubmitting = false
                    }
                },
                enabled = isConfirmEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Charcoal,
                    contentColor = Color.White,
                    disabledContainerColor = WarmGray,
                    disabledContentColor = DisabledColor
                )
            ) {
                Text("Confirm & Try On", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            val statusText = when {
                SessionManager.isGuest -> "Guest mode (Free preview)"
                !hasValidPhoto -> "Add a photo above to continue"
                !hasSufficientCredits -> "No credits remaining (0 available)"
                else -> "1 Credit will be used (${SessionManager.availableCredits} remaining)"
            }
            Text(
                text = statusText,
                fontSize = 12.sp,
                color = SoftCharcoal,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProcessingScreen(navController: NavController) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var isCompletedSuccessfully by remember { mutableStateOf(false) }

    // Ensure any held credit is released if Processing is cancelled or disposed before completion
    DisposableEffect(Unit) {
        onDispose {
            if (!isCompletedSuccessfully) {
                SessionManager.releaseHeldCredit()
            }
        }
    }

    val tips = remember {
        listOf(
            "Style Tip\nMonochrome outfits can create a clean, streamlined look.",
            "OnMe Tip\nTry the same outfit with another photo to compare the result.",
            "Style Fact\nA tailored blazer instantly adds structure to any relaxed fit.",
            "Style Tip\nLinen works beautifully with simple accessories for an effortless look.",
            "Color Harmony\nNeutral tones pair effortlessly with deep forest accents."
        )
    }

    var currentTipIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        // Rotate tips
        launch {
            while (true) {
                delay(3000)
                currentTipIndex = (currentTipIndex + 1) % tips.size
            }
        }

        // Simulate AI Processing time
        launch {
            delay(4500)
            if (TryOnManager.generatedResultImageUri.isEmpty()) {
                TryOnManager.generatedResultImageUri = TryOnManager.selectedProductImage
            }
            TryOnManager.showWatermark = true
            
            // Mark completed and finalize the held credit exactly once
            SessionManager.consumeHeldCredit()
            isCompletedSuccessfully = true

            navController.navigate(Screen.Result.route) {
                popUpTo(Screen.TryOn.route) { inclusive = true }
            }
        }
    }

    BackHandler {
        showCancelDialog = true
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel processing?", color = Charcoal, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to cancel? Your try-on progress will be stopped.", color = SoftCharcoal) },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        SessionManager.releaseHeldCredit()
                        navController.navigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White)
                ) {
                    Text("Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Waiting", color = SoftCharcoal)
                }
            },
            containerColor = WarmIvory
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // AI Animation Box
        Box(
            modifier = Modifier
                .size(190.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(36.dp))
                .background(DeepForest.copy(alpha = alpha))
                .border(2.dp, DeepForest, RoundedCornerShape(36.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI",
                tint = Color.White,
                modifier = Modifier.size(68.dp)
            )
        }

        Spacer(modifier = Modifier.height(44.dp))

        Text(
            text = "Creating your look...",
            color = Charcoal,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Fitting the outfit to your photo with AI.",
            color = SoftCharcoal,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(44.dp))

        // Rotating Tip Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(96.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = OffWhite),
            border = BorderStroke(1.dp, WarmGray)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = tips[currentTipIndex],
                    color = SoftCharcoal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ResultScreen(navController: NavController) {
    val context = LocalContext.current
    var showAccountPrompt by remember { mutableStateOf(false) }

    if (showAccountPrompt) {
        AlertDialog(
            onDismissRequest = { showAccountPrompt = false },
            title = {
                Text("Save your style", fontWeight = FontWeight.Bold, color = Charcoal)
            },
            text = {
                Text("Create an account to save your generated looks and track products.", color = SoftCharcoal)
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
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Charcoal)
            }
            Text("Your New Look", color = Charcoal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Hero AI-Generated Result Image
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(OffWhite)
                .border(1.dp, WarmGray, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(TryOnManager.generatedResultImageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Your New Look",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (TryOnManager.showWatermark) {
                // Watermark pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "OnMe AI",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Product Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            formatter.maximumFractionDigits = 0
            val priceStr = formatter.format(TryOnManager.selectedProductPrice)

            Text(
                text = TryOnManager.selectedProductBrand.uppercase(),
                fontSize = 12.sp,
                color = SoftCharcoal,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TryOnManager.selectedProductName,
                    fontSize = 18.sp,
                    color = Charcoal,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = priceStr,
                    fontSize = 18.sp,
                    color = Charcoal,
                    fontWeight = FontWeight.Bold
                )
            }

            // Rating / Review row ONLY when real non-null merchant data exists
            if (TryOnManager.rating != null && TryOnManager.reviewCount != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val sourceText = TryOnManager.ratingSource?.let { " · $it" } ?: ""
                    Text(
                        text = "${TryOnManager.rating} (${TryOnManager.reviewCount})$sourceText",
                        fontSize = 12.sp,
                        color = SoftCharcoal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Compact Actions: Save, Download, Share, 🔔 Price Drop
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (SessionManager.isGuest) {
                        showAccountPrompt = true
                    } else {
                        val newResult = TryOnResult(
                            id = "result_${System.currentTimeMillis()}",
                            userPhoto = TryOnManager.selectedUserPhotoUri,
                            outfitImage = TryOnManager.selectedProductImage,
                            resultImage = TryOnManager.generatedResultImageUri,
                            createdAt = "Just now",
                            isFavourite = OnMeStyleRepository.isFavourite(TryOnManager.selectedProductId),
                            productId = TryOnManager.selectedProductId,
                            productName = TryOnManager.selectedProductName,
                            productBrand = TryOnManager.selectedProductBrand,
                            productPrice = TryOnManager.selectedProductPrice,
                            cardHeight = 240
                        )
                        OnMeStyleRepository.saveResult(newResult)
                        Toast.makeText(context, "Saved to your Looks!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Charcoal),
                border = BorderStroke(1.dp, WarmGray)
            ) {
                Text("Save", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
            Button(
                onClick = {
                    Toast.makeText(context, "Downloaded to Gallery!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1.15f)
                    .height(44.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Charcoal),
                border = BorderStroke(1.dp, WarmGray)
            ) {
                Text("Download", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Check out my new look on OnMe! Outfit: ${TryOnManager.selectedProductName} (${TryOnManager.selectedProductBrand})")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Look"))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Charcoal),
                border = BorderStroke(1.dp, WarmGray)
            ) {
                Text("Share", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
            Button(
                onClick = {
                    if (SessionManager.isGuest) {
                        showAccountPrompt = true
                    } else {
                        val isNowTracked = OnMeStyleRepository.togglePriceTracking(
                            productId = TryOnManager.selectedProductId,
                            productName = TryOnManager.selectedProductName,
                            merchant = TryOnManager.selectedProductBrand,
                            price = TryOnManager.selectedProductPrice,
                            imageUrl = TryOnManager.selectedProductImage
                        )
                        val msg = if (isNowTracked) "Price tracking enabled!" else "Price tracking disabled"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1.4f)
                    .height(44.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (TryOnManager.isPriceTracked) DeepForest.copy(alpha = 0.12f) else White,
                    contentColor = if (TryOnManager.isPriceTracked) DeepForest else Charcoal
                ),
                border = BorderStroke(1.dp, if (TryOnManager.isPriceTracked) DeepForest else WarmGray)
            ) {
                Text(
                    text = if (TryOnManager.isPriceTracked) "🔔 Price Drop ✓" else "🔔 Price Drop",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Primary Purchase CTA Button
        Button(
            onClick = {
                Toast.makeText(context, "Opening store for ${TryOnManager.selectedProductName}...", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White)
        ) {
            Text("Buy ↗", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Watermark Removal Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (TryOnManager.showWatermark) {
                Text("Remove Watermark? ", fontSize = 13.sp, color = SoftCharcoal)
                Text(
                    text = "Watch Ad",
                    fontSize = 13.sp,
                    color = DeepForest,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Watching Ad...", Toast.LENGTH_SHORT).show()
                        TryOnManager.showWatermark = false
                    }
                )
            } else {
                Text("Watermark Removed ✨", fontSize = 13.sp, color = Charcoal, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try Another", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = SoftCharcoal)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
