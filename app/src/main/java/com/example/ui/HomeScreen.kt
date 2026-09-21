package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Product
import com.example.data.model.HeroLook
import com.example.data.repository.ProductRepository
import com.example.ui.motion.TiHinAnimatedCreditsPill
import com.example.ui.motion.TiHinAnimatedHeartIcon
import com.example.ui.motion.tihinButtonPress
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

typealias OutfitShowcaseData = HeroLook

data class TrendingLook(
    val id: String,
    val title: String,
    val imageUrl: String,
    val brand: String,
    val brandLogo: String? = null,
    val price: Double,
    val tryOnCount: String? = null
) {
    companion object {
        fun fromProduct(product: Product): TrendingLook = TrendingLook(
            id = product.id,
            title = product.name,
            imageUrl = product.primaryImageUrl,
            brand = product.brand,
            price = product.price
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToDiscover: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val productRepo = remember { ProductRepository.get() }
    var catalogVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val result = productRepo.refreshCatalog()
        if (result.isSuccess) {
            catalogVersion++
        }
    }

    val showcaseDataList = remember(catalogVersion) { 
        productRepo.getHeroLooks().filter { look ->
            val product = productRepo.getProductById(look.productId)
            val hangerImg = look.hangerImage ?: product?.productImages?.getOrNull(1)
            val wornImg = look.wornImage ?: product?.productImages?.getOrNull(0) ?: product?.primaryImageUrl
            hangerImg != null && wornImg != null && hangerImg != wornImg
        }
    }

    val pagerState = rememberPagerState(pageCount = { showcaseDataList.size })
    LaunchedEffect(pagerState, showcaseDataList.size) {
        if (showcaseDataList.size > 1) {
            while (true) {
                delay(4000)
                if (!pagerState.isScrollInProgress) {
                    val nextPage = (pagerState.currentPage + 1) % showcaseDataList.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }
    }

    val trendingLooks = remember(catalogVersion) {
        val trendingProducts = productRepo.getTrendingProducts()
        trendingProducts.map { TrendingLook.fromProduct(it) }
    }

    // Most Loved reactive state
    val mostLovedProducts = remember(TiHinStyleRepository.favouriteProductIds, catalogVersion) {
        TiHinStyleRepository.favouriteProductIds.mapNotNull { id ->
            productRepo.getProductById(id)?.let { TrendingLook.fromProduct(it) }
        }
    }

    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                TryOnManager.updateUserPhoto(tempCameraUri.toString(), context)
                if (TryOnManager.selectedProductId == null) {
                    val defaultProduct = productRepo.getHeroLooks().firstOrNull()?.let {
                        productRepo.getProductById(it.productId)
                    } ?: productRepo.getTrendingProducts().firstOrNull()
                    if (defaultProduct != null) {
                        TryOnManager.selectedProductId = defaultProduct.id
                        TryOnManager.selectedProductName = defaultProduct.name
                        TryOnManager.selectedProductBrand = defaultProduct.brand
                        TryOnManager.selectedProductPrice = defaultProduct.price
                        TryOnManager.selectedProductImage = defaultProduct.primaryImageUrl
                    }
                }
                navController.navigate(Screen.TryOn.route)
            }
            tempCameraUri = null
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                TryOnManager.updateUserPhoto(uri.toString(), context)
                if (TryOnManager.selectedProductId == null) {
                    val defaultProduct = productRepo.getHeroLooks().firstOrNull()?.let {
                        productRepo.getProductById(it.productId)
                    } ?: productRepo.getTrendingProducts().firstOrNull()
                    if (defaultProduct != null) {
                        TryOnManager.selectedProductId = defaultProduct.id
                        TryOnManager.selectedProductName = defaultProduct.name
                        TryOnManager.selectedProductBrand = defaultProduct.brand
                        TryOnManager.selectedProductPrice = defaultProduct.price
                        TryOnManager.selectedProductImage = defaultProduct.primaryImageUrl
                    }
                }
                navController.navigate(Screen.TryOn.route)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen")
            .background(WarmIvory)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp, bottom = 28.dp)
    ) {
        // 1. HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TiHin",
                        fontFamily = EditorialSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Charcoal,
                        letterSpacing = (-0.5).sp
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = DeepForest,
                        modifier = Modifier
                            .size(15.dp)
                            .offset(x = 3.dp, y = (-6).dp)
                    )
                }
                Text(
                    text = stringResource(R.string.tagline),
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = SoftCharcoal,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.offset(y = (-2).dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!SessionManager.isGuest) {
                    TiHinAnimatedCreditsPill(
                        credits = SessionManager.credits,
                        onClick = { navController.navigate(Screen.TriesCredits.route) }
                    )
                }
                val notifSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceColor, CircleShape)
                        .border(1.dp, CardBorder, CircleShape)
                        .tihinButtonPress(notifSource, pressedScale = 0.92f)
                        .clickable(
                            interactionSource = notifSource,
                            indication = null
                        ) { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Charcoal,
                        modifier = Modifier.size(18.dp)
                    )
                    // Notification unread dot
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .size(7.dp)
                            .background(DeepForest, CircleShape)
                            .border(1.dp, SurfaceColor, CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. HERO SHOWCASE: SEE THE MAGIC
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "See the Magic",
                    fontFamily = EditorialSerif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = DeepForest,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Carousel with side-by-side comparison images
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("see_the_magic_carousel")
            ) { page ->
                val currentShowcase = showcaseDataList[page]
                val product = remember(currentShowcase.productId) { ProductRepository.get().getProductById(currentShowcase.productId) }

                if (product != null) {
                    val hangerImg = currentShowcase.hangerImage ?: product.productImages.getOrNull(1)
                    val wornImg = currentShowcase.wornImage ?: product.productImages.getOrNull(0) ?: product.primaryImageUrl

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SurfaceColor,
                        border = BorderStroke(1.dp, CardBorder),
                        shadowElevation = SubtleCardElevation,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            ) {
                                if (hangerImg != null && wornImg != null && hangerImg != wornImg) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Left Image: Product outfit / hanger presentation
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(SurfaceVariantColor)
                                                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(hangerImg)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Selected product presentation",
                                                contentScale = ContentScale.Crop,
                                                alignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            // Editorial context pill
                                            Surface(
                                                color = Charcoal.copy(alpha = 0.78f),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = "PIECE",
                                                    fontFamily = Inter,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.2.sp,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        // Right Image: Model fitted with the same outfit
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(SurfaceVariantColor)
                                                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(wornImg)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Fitted view on model",
                                                contentScale = ContentScale.Crop,
                                                alignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            // Editorial context pill
                                            Surface(
                                                color = DeepForest.copy(alpha = 0.88f),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = "ON MODEL",
                                                    fontFamily = Inter,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.2.sp,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Center transition arrow badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(38.dp)
                                            .shadow(2.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.15f))
                                            .background(SurfaceColor, CircleShape)
                                            .border(1.dp, CardBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Try-on arrow",
                                            tint = DeepForest,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Editorial product hierarchy: Brand -> Name -> Price -> CTA
                            val heroPriceFormatted = remember(product.price) {
                                NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
                                    maximumFractionDigits = 0
                                }.format(product.price)
                            }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = product.brand.uppercase(),
                                    style = BrandTagStyle,
                                    color = DeepForest,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.3.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = product.name,
                                        fontSize = 16.sp,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Charcoal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = heroPriceFormatted,
                                        fontSize = 15.sp,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Bold,
                                        color = Charcoal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Polished "Try this look" CTA button
                            val tryThisLookSource = remember { MutableInteractionSource() }
                            Button(
                                onClick = {
                                    TryOnManager.selectedProductId = product.id
                                    TryOnManager.selectedProductName = product.name
                                    TryOnManager.selectedProductBrand = product.brand
                                    TryOnManager.selectedProductPrice = product.price
                                    TryOnManager.selectedProductImage = product.primaryImageUrl
                                    TryOnManager.generatedResultImageUri = wornImg ?: product.primaryImageUrl
                                    navController.navigate(Screen.TryOn.route)
                                },
                                interactionSource = tryThisLookSource,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Charcoal,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("try_this_look_button")
                                    .tihinButtonPress(tryThisLookSource)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ChampagneGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Try this look",
                                    fontSize = 14.sp,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.2.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Carousel Dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(showcaseDataList.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 20.dp else 6.dp,
                        animationSpec = tween(220, easing = FastOutSlowInEasing),
                        label = "carousel_dot_width_$iteration"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = if (isSelected) DeepForest else BorderColor,
                        animationSpec = tween(220),
                        label = "carousel_dot_color_$iteration"
                    )
                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    if (iteration < showcaseDataList.size - 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. ACTION BUTTONS: Take Photo & Choose Photo (Coordinated Pair)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Take Photo (Primary Action)
            val takePhotoSource = remember { MutableInteractionSource() }
            Surface(
                onClick = {
                    val uri = TryOnManager.createTempCameraUri(context)
                    if (uri != null) {
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    }
                },
                interactionSource = takePhotoSource,
                shape = RoundedCornerShape(16.dp),
                color = Charcoal,
                shadowElevation = SubtleCardElevation,
                modifier = Modifier
                    .weight(1f)
                    .height(74.dp)
                    .testTag("take_photo_button")
                    .tihinButtonPress(takePhotoSource)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take Photo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "Take Photo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Try on any outfit",
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.82f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Choose Photo (Secondary Action)
            val gallerySource = remember { MutableInteractionSource() }
            Surface(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                interactionSource = gallerySource,
                shape = RoundedCornerShape(16.dp),
                color = SurfaceColor,
                border = BorderStroke(1.dp, CardBorder),
                shadowElevation = SubtleCardElevation,
                modifier = Modifier
                    .weight(1f)
                    .height(74.dp)
                    .testTag("choose_photo_button")
                    .tihinButtonPress(gallerySource)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(SurfaceVariantColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Choose Photo",
                            tint = DeepForest,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "Choose Photo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "From your gallery",
                            fontSize = 11.5.sp,
                            color = SoftCharcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 4. TRENDING LOOKS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trending_looks_section"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trending Looks",
                    fontFamily = EditorialSerif,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    letterSpacing = (-0.2).sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToDiscover() }
                ) {
                    Text("View All", fontSize = 13.sp, color = DeepForest, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = DeepForest,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(trendingLooks, key = { it.id }) { item ->
                    TrendingLookCard(
                        data = item,
                        isFavourite = TiHinStyleRepository.isFavourite(item.id),
                        onToggleFavourite = {
                            TiHinStyleRepository.toggleFavourite(
                                productId = item.id,
                                productName = item.title,
                                merchant = item.brand,
                                price = item.price,
                                imageUrl = item.imageUrl
                            )
                        },
                        onTryOn = {
                            TryOnManager.selectedProductId = item.id
                            TryOnManager.selectedProductName = item.title
                            TryOnManager.selectedProductBrand = item.brand
                            TryOnManager.selectedProductPrice = item.price
                            TryOnManager.selectedProductImage = item.imageUrl
                            navController.navigate(Screen.TryOn.route)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        
        // 5. MOST LOVED SECTION
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Most Loved",
                    fontFamily = EditorialSerif,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    letterSpacing = (-0.2).sp
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            if (mostLovedProducts.isEmpty()) {
                // Empty state for Most Loved
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceColor,
                    border = BorderStroke(1.dp, CardBorder),
                    shadowElevation = SubtleCardElevation,
                    modifier = Modifier.fillMaxWidth().height(92.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nothing loved yet",
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Charcoal
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Tap the heart on a style you love.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = SoftCharcoal
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(mostLovedProducts, key = { it.id }) { item ->
                        TrendingLookCard(
                            data = item,
                            isFavourite = TiHinStyleRepository.isFavourite(item.id),
                            onToggleFavourite = {
                                TiHinStyleRepository.toggleFavourite(
                                    productId = item.id,
                                    productName = item.title,
                                    merchant = item.brand,
                                    price = item.price,
                                    imageUrl = item.imageUrl
                                )
                            },
                            onTryOn = {
                                TryOnManager.selectedProductId = item.id
                                TryOnManager.selectedProductName = item.title
                                TryOnManager.selectedProductBrand = item.brand
                                TryOnManager.selectedProductPrice = item.price
                                TryOnManager.selectedProductImage = item.imageUrl
                                navController.navigate(Screen.TryOn.route)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendingLookCard(
    data: TrendingLook,
    isFavourite: Boolean,
    onToggleFavourite: () -> Unit,
    onTryOn: () -> Unit
) {
    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = 0
        }
    }
    val formattedPrice = formatter.format(data.price)
    
    // Responsive sizing: Target ~1.7 cards on normal phones
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cardWidth = (screenWidth * 0.52f).coerceIn(175.dp, 210.dp)

    Card(
        onClick = onTryOn,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = SubtleCardElevation),
        modifier = Modifier
            .width(cardWidth)
            .testTag("trending_look_card_${data.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // [ PRODUCT IMAGE ]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(SurfaceVariantColor)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(data.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = data.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Heart button: positioned inside the image, top-right
                val favSource = remember { MutableInteractionSource() }
                Surface(
                    onClick = onToggleFavourite,
                    interactionSource = favSource,
                    shape = CircleShape,
                    color = SurfaceColor.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .testTag("trending_look_heart_${data.id}")
                        .tihinButtonPress(favSource, pressedScale = 0.92f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        TiHinAnimatedHeartIcon(
                            isFavourite = isFavourite,
                            contentDescription = if (isFavourite) "Remove from favourites" else "Save to favourites",
                            activeTint = DeepForest,
                            inactiveTint = PrimaryText,
                            iconSize = 17.dp
                        )
                    }
                }

                if (!data.tryOnCount.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "✨ ${data.tryOnCount} tried",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Below image: Brand & Price, Product Name, Try On
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!data.brandLogo.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(data.brandLogo)
                                .crossfade(true)
                                .build(),
                            contentDescription = data.brand,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(14.dp)
                                .widthIn(max = 55.dp)
                        )
                    } else {
                        Text(
                            text = data.brand.uppercase(),
                            style = BrandTagStyle,
                            color = DeepForest,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = formattedPrice,
                        fontSize = 13.5.sp,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = data.title,
                    fontSize = 13.sp,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    color = Charcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val cardTryOnSource = remember { MutableInteractionSource() }
                    Button(
                        onClick = onTryOn,
                        interactionSource = cardTryOnSource,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepForest,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("trending_look_try_on_${data.id}")
                            .tihinButtonPress(cardTryOnSource)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Try On",
                            fontSize = 12.sp,
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
