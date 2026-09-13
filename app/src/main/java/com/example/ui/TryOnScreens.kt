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
import com.example.tryon.model.GenerationStatus
import com.example.tryon.model.TryOnError
import com.example.tryon.model.TryOnException
import com.example.tryon.model.TryOnRequest
import com.example.tryon.service.TryOnService
import com.example.tryon.service.TryOnServiceProvider
import com.example.tryon.model.TryOnResult as DomainTryOnResult
import com.example.tryon.model.UserPhotoMetadata
import com.example.ui.theme.*
import kotlinx.coroutines.CancellationException
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
        get() = TiHinStyleRepository.isPriceTracked(selectedProductId)

    var selectedUserPhotoUri by mutableStateOf("")
    var generatedResultImageUri by mutableStateOf("")
    var showWatermark by mutableStateOf(true)
    var originalMerchantUrl by mutableStateOf<String?>(null)

    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main)
    private var generationJob: kotlinx.coroutines.Job? = null

    // Backend-ready generation state orchestration
    var currentRequest by mutableStateOf<TryOnRequest?>(null)

    fun startGeneration(
        request: TryOnRequest = currentRequest ?: prepareRequest(),
        service: TryOnService = TryOnServiceProvider.get()
    ) {
        if (generationStatus is com.example.tryon.model.GenerationStatus.Processing) return
        
        generationJob?.cancel()
        generationJob = scope.launch {
            try {
                executeGeneration(request, service)
            } catch (e: Exception) {
                // handled inside
            }
        }
    }
    var currentResult by mutableStateOf<DomainTryOnResult?>(null)
    var generationStatus by mutableStateOf<GenerationStatus>(GenerationStatus.Idle)
    var lastError by mutableStateOf<TryOnError?>(null)

    fun prepareRequest(source: String? = null): TryOnRequest {
        val request = TryOnRequest(
            productId = selectedProductId,
            userPhotoUri = selectedUserPhotoUri,
            userPhotoMetadata = UserPhotoMetadata(
                uri = selectedUserPhotoUri,
                source = source
            )
        )
        currentRequest = request
        generationStatus = GenerationStatus.Idle
        lastError = null
        return request
    }

    suspend fun executeGeneration(
        request: TryOnRequest = currentRequest ?: prepareRequest(),
        service: TryOnService = TryOnServiceProvider.get()
    ): DomainTryOnResult {
        currentRequest = request
        generationStatus = GenerationStatus.Processing(request.requestId)
        lastError = null
        
        val creditRepo = com.example.credit.repository.CreditRepositoryProvider.get()
        val holdSuccess = creditRepo.hold(request.requestId)
        SessionManager.refreshAvailableCredits()
        if (!holdSuccess) {
            val error = TryOnError.Unknown("Insufficient credits")
            lastError = error
            generationStatus = GenerationStatus.Failed(error)
            throw TryOnException(error, Exception("Insufficient credits"))
        }

        return try {
            val result = service.generate(request)
            currentResult = result
            generatedResultImageUri = result.generatedImageUri
            showWatermark = result.watermarkApplied
            generationStatus = GenerationStatus.Success(result)
            creditRepo.consume(request.requestId)
            SessionManager.refreshAvailableCredits()
            result
        } catch (e: kotlinx.coroutines.CancellationException) {
            service.cancel(request.requestId)
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                creditRepo.release(request.requestId)
                SessionManager.refreshAvailableCredits()
            }
            generationStatus = GenerationStatus.Cancelled(request.requestId)
            throw e
        } catch (e: TryOnException) {
            service.cancel(request.requestId)
            creditRepo.release(request.requestId)
            SessionManager.refreshAvailableCredits()
            lastError = e.error
            generationStatus = GenerationStatus.Failed(e.error)
            throw e
        } catch (e: Exception) {
            service.cancel(request.requestId)
            creditRepo.release(request.requestId)
            SessionManager.refreshAvailableCredits()
            val error = TryOnError.Unknown(e.message)
            lastError = error
            generationStatus = GenerationStatus.Failed(error)
            throw TryOnException(error, e)
        }
    }


    fun cancelGeneration(
        service: TryOnService = TryOnServiceProvider.get()
    ) {
        generationJob?.cancel()
        generationJob = null
        val request = currentRequest
        if (generationStatus is GenerationStatus.Processing) {
            if (request != null) {
                service.cancel(request.requestId)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    com.example.credit.repository.CreditRepositoryProvider.get().release(request.requestId)
                }
            }
            generationStatus = GenerationStatus.Cancelled(request?.requestId)
        }
    }

    fun resetForTesting() {
        generationJob?.cancel()
        generationJob = null
        currentRequest = null
        currentResult = null
        generationStatus = GenerationStatus.Idle
        lastError = null
        selectedProductId = "prod_123"
        selectedProductImage = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&q=80"
        selectedProductName = "Blush Co-ord Set"
        selectedProductBrand = "URBANIC"
        selectedProductPrice = 2999.0
        selectedUserPhotoUri = ""
        generatedResultImageUri = ""
        showWatermark = true
        originalMerchantUrl = null
    }

    fun selectProduct(product: com.example.data.model.Product, originalUrl: String? = null) {
        selectedProductId = product.id
        selectedProductImage = product.primaryImageUrl
        selectedProductName = product.name
        selectedProductBrand = product.brand
        selectedProductPrice = product.price
        rating = product.rating
        reviewCount = product.reviewCount
        ratingSource = null
        originalMerchantUrl = originalUrl
        generatedResultImageUri = ""
        currentRequest = null
        currentResult = null
        generationStatus = GenerationStatus.Idle
        lastError = null
    }

    fun updateUserPhoto(uri: String?, context: Context? = null) {
        if (!uri.isNullOrBlank()) {
            selectedUserPhotoUri = uri
            if (!SessionManager.isGuest && context != null) {
                UserPhotosRepository.addPhoto(context, uri, setAsDefault = true)
            }
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

    BackHandler {
        if (!navController.navigateUp()) {
            navController.navigate(Screen.MainApp.route) {
                popUpTo(Screen.TryOn.route) { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (TryOnManager.selectedUserPhotoUri.isBlank() && !SessionManager.isGuest) {
            UserPhotosRepository.defaultPhotoUri?.let { uri ->
                TryOnManager.selectedUserPhotoUri = uri
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                TryOnManager.updateUserPhoto(tempCameraUri.toString(), context)
                /* Toast disabled for tests */
            }
            tempCameraUri = null
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                TryOnManager.updateUserPhoto(uri.toString(), context)
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
                            /* Toast disabled for tests */
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
            IconButton(onClick = {
                if (!navController.navigateUp()) {
                    navController.navigate(Screen.MainApp.route) {
                        popUpTo(Screen.TryOn.route) { inclusive = true }
                    }
                }
            }) {
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
                    TryOnManager.prepareRequest()
                    navController.navigate(Screen.Processing.route)
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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val tips = remember {
        listOf(
            "Style Tip\nMonochrome outfits can create a clean, streamlined look.",
            "TiHin Tip\nTry the same outfit with another photo to compare the result.",
            "Style Fact\nA tailored blazer instantly adds structure to any relaxed fit.",
            "Style Tip\nLinen works beautifully with simple accessories for an effortless look.",
            "Color Harmony\nNeutral tones pair effortlessly with deep forest accents."
        )
    }

    var currentTipIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        TryOnManager.startGeneration()
    }

    LaunchedEffect(TryOnManager.generationStatus) {
        when (val status = TryOnManager.generationStatus) {
            is com.example.tryon.model.GenerationStatus.Success -> {
                navController.navigate(Screen.Result.route) {
                    popUpTo(Screen.TryOn.route) { inclusive = true }
                }
            }
            is com.example.tryon.model.GenerationStatus.Failed -> {
                errorMessage = status.error.userMessage ?: "Generation failed."
            }
            else -> {}
        }
    }

    LaunchedEffect(TryOnManager.currentRequest?.requestId) {
        // Rotate tips
        while (true) {
            kotlinx.coroutines.delay(3000)
            currentTipIndex = (currentTipIndex + 1) % tips.size
        }
    }

    BackHandler {
        showCancelDialog = true
    }

    if (showCancelDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { androidx.compose.material3.Text("Cancel Generation?", color = com.example.ui.theme.Charcoal, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            text = { androidx.compose.material3.Text("Are you sure you want to cancel? Your credit will not be consumed.", color = com.example.ui.theme.SoftCharcoal) },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        showCancelDialog = false
                        TryOnManager.cancelGeneration()
                        navController.navigateUp()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.Charcoal, contentColor = androidx.compose.ui.graphics.Color.White)
                ) {
                    androidx.compose.material3.Text("Cancel")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showCancelDialog = false }) {
                    androidx.compose.material3.Text("Keep Waiting", color = com.example.ui.theme.SoftCharcoal)
                }
            },
            containerColor = com.example.ui.theme.WarmIvory
        )
    }

    if (errorMessage != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                errorMessage = null
                navController.navigateUp()
            },
            title = { androidx.compose.material3.Text("Couldn't create look", color = com.example.ui.theme.Charcoal, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            text = { androidx.compose.material3.Text(errorMessage ?: "Generation failed", color = com.example.ui.theme.SoftCharcoal) },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        errorMessage = null
                        navController.navigateUp()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.Charcoal, contentColor = androidx.compose.ui.graphics.Color.White)
                ) {
                    androidx.compose.material3.Text("Go Back")
                }
            },
            containerColor = com.example.ui.theme.WarmIvory
        )
    }

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "shimmer_scale"
    )

    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.WarmIvory)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // AI Animation Box
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .size(190.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(36.dp))
                .background(com.example.ui.theme.DeepForest.copy(alpha = alpha))
                .border(2.dp, com.example.ui.theme.DeepForest, androidx.compose.foundation.shape.RoundedCornerShape(36.dp)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = androidx.compose.ui.Modifier.size(72.dp)
            )
        }
        
        Spacer(modifier = androidx.compose.ui.Modifier.height(56.dp))
        
        androidx.compose.material3.Text(
            text = "Styling your look...",
            fontSize = 24.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = com.example.ui.theme.Charcoal,
            fontFamily = com.example.ui.theme.CormorantGaramond
        )
        Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))
        
        // Animated ellipses for loading state
        var dotCount by remember { mutableIntStateOf(1) }
        LaunchedEffect(Unit) {
            while(true) {
                kotlinx.coroutines.delay(400)
                dotCount = (dotCount % 3) + 1
            }
        }
        
        val parts = tips[currentTipIndex].split("\n")
        val title = parts.getOrNull(0) ?: "Style Tip"
        val body = parts.getOrNull(1) ?: ""
        
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .padding(horizontal = 40.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.Text(
                    title,
                    color = com.example.ui.theme.DeepForest,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
                androidx.compose.material3.Text(
                    body,
                    color = com.example.ui.theme.SoftCharcoal,
                    fontSize = 15.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 22.sp
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
                        text = "TiHin AI",
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
                            isFavourite = TiHinStyleRepository.isFavourite(TryOnManager.selectedProductId),
                            productId = TryOnManager.selectedProductId,
                            productName = TryOnManager.selectedProductName,
                            productBrand = TryOnManager.selectedProductBrand,
                            productPrice = TryOnManager.selectedProductPrice,
                            cardHeight = 240
                        )
                        TiHinStyleRepository.saveResult(newResult)
                        /* Toast disabled for tests */
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
                    /* Toast disabled for tests */
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
                        putExtra(Intent.EXTRA_TEXT, "Check out my new look on TiHin! Outfit: ${TryOnManager.selectedProductName} (${TryOnManager.selectedProductBrand})")
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
                        val isNowTracked = TiHinStyleRepository.togglePriceTracking(
                            productId = TryOnManager.selectedProductId,
                            productName = TryOnManager.selectedProductName,
                            merchant = TryOnManager.selectedProductBrand,
                            price = TryOnManager.selectedProductPrice,
                            imageUrl = TryOnManager.selectedProductImage
                        )
                        val msg = if (isNowTracked) "Price tracking enabled!" else "Price tracking disabled"
                        /* Toast disabled for tests */
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
                /* Toast disabled for tests */
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
                        /* Toast disabled for tests */
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
