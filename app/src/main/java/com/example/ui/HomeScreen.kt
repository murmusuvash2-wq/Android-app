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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

data class HeroLook(
    val id: String,
    val title: String,
    val primaryImage: String,
    val hangerImage: String? = null,
    val wornImage: String? = null,
    val brand: String,
    val brandLogo: String? = null,
    val price: Double
) {
    // Backward-compatibility properties with hangerImage fallback
    val productImageUrl: String get() = hangerImage ?: primaryImage
    val modelImageUrl: String get() = wornImage ?: primaryImage
}

typealias OutfitShowcaseData = HeroLook

data class TrendingLook(
    val id: String,
    val title: String,
    val imageUrl: String,
    val brand: String,
    val brandLogo: String? = null,
    val price: Double,
    val tryOnCount: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToDiscover: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val showcaseDataList = remember {
        listOf(
            HeroLook(
                id = "hero_0",
                primaryImage = "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=600&q=80",
                wornImage = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=600&q=80",
                title = "Forest Floral Dress",
                brand = "ZARA",
                price = 3499.0
            ),
            HeroLook(
                id = "hero_1",
                primaryImage = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=600&q=80",
                wornImage = "https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=600&q=80",
                title = "Blue Floral Smocked Dress",
                brand = "H&M",
                price = 2999.0
            ),
            HeroLook(
                id = "hero_2",
                primaryImage = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80",
                wornImage = "https://images.unsplash.com/photo-1509631179647-0177331693ae?auto=format&fit=crop&w=600&q=80",
                title = "Beige Linen Co-ord Set",
                brand = "MANGO",
                price = 4599.0
            ),
            HeroLook(
                id = "hero_3",
                primaryImage = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=600&q=80",
                wornImage = "https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?auto=format&fit=crop&w=600&q=80",
                title = "Navy Knit Polo & Trousers",
                brand = "MASSIMO DUTTI",
                price = 5999.0
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { showcaseDataList.size })
    LaunchedEffect(pagerState) {
        while (true) {
            delay(3500)
            val nextPage = (pagerState.currentPage + 1) % showcaseDataList.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    val trendingLooks = remember {
        listOf(
            TrendingLook(
                id = "1",
                title = "Linen Day Dress",
                imageUrl = "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=400&q=80",
                brand = "URBANIC",
                price = 2499.0,
                tryOnCount = null
            ),
            TrendingLook(
                id = "2",
                title = "Knit Polo & Chinos",
                imageUrl = "https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?auto=format&fit=crop&w=400&q=80",
                brand = "ZARA",
                price = 3999.0,
                tryOnCount = null
            ),
            TrendingLook(
                id = "3",
                title = "Everyday Denim & Tee",
                imageUrl = "https://images.unsplash.com/photo-1558769132-cb1aea458c5e?auto=format&fit=crop&w=400&q=80",
                brand = "LEVI'S",
                price = 2899.0,
                tryOnCount = null
            ),
            TrendingLook(
                id = "4",
                title = "Blush Co-ord Set",
                imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=400&q=80",
                brand = "H&M",
                price = 3299.0,
                tryOnCount = null
            )
        )
    }

    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                TryOnManager.updateUserPhoto(tempCameraUri.toString())
                Toast.makeText(context, "Try-On photo updated", Toast.LENGTH_SHORT).show()
            }
            tempCameraUri = null
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                TryOnManager.updateUserPhoto(uri.toString())
                Toast.makeText(context, "Try-On photo updated", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = SpacingXl, bottom = SpacingXl),
        verticalArrangement = Arrangement.spacedBy(SpacingXl)
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
                        text = "OnMe",
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = Charcoal,
                        letterSpacing = (-1).sp
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = DeepForest,
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = 3.dp, y = (-5).dp)
                    )
                }
                Text(
                    text = "See it on you.",
                    fontSize = 12.sp,
                    color = SoftCharcoal,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.offset(y = (-4).dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!SessionManager.isGuest) {
                    Surface(
                        onClick = { navController.navigate(Screen.TriesCredits.route) },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, WarmGray),
                        modifier = Modifier.testTag("tries_counter_pill")
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
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, WarmGray, CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Charcoal,
                        modifier = Modifier.size(18.dp)
                    )
                    // Notification dot
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .size(7.dp)
                            .background(DeepForest, CircleShape)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
            }
        }

        // 2. HERO SHOWCASE
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "See the magic",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = DeepForest,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Carousel with side-by-side images
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val currentShowcase = showcaseDataList[page]
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Left Image: Product outfit
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currentShowcase.productImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Outfit product view",
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.TopCenter,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(OffWhite)
                                    .border(1.dp, WarmGray, RoundedCornerShape(16.dp))
                            )
                            // Right Image: Model fitted with outfit
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currentShowcase.modelImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Fitted view",
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.TopCenter,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(OffWhite)
                                    .border(1.dp, WarmGray, RoundedCornerShape(16.dp))
                            )
                        }

                        // Center arrow badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(44.dp)
                                .shadow(8.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.15f))
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Try-on arrow",
                                tint = DeepForest,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Info and CTA row (compact, showing title, price, and CTA)
                    val heroPriceFormatted = remember(currentShowcase.price) {
                        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
                            maximumFractionDigits = 0
                        }.format(currentShowcase.price)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currentShowcase.productImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OffWhite)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentShowcase.title,
                                    fontSize = 14.sp,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = heroPriceFormatted,
                                    fontSize = 13.sp,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SoftCharcoal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                TryOnManager.selectedProductId = currentShowcase.id
                                TryOnManager.selectedProductName = currentShowcase.title
                                TryOnManager.selectedProductBrand = currentShowcase.brand
                                TryOnManager.selectedProductPrice = currentShowcase.price
                                TryOnManager.selectedProductImage = currentShowcase.primaryImage
                                TryOnManager.generatedResultImageUri = currentShowcase.wornImage ?: currentShowcase.primaryImage
                                navController.navigate(Screen.TryOn.route)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Try this look", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
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
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isSelected) 18.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) DeepForest else WarmGray)
                    )
                    if (iteration < showcaseDataList.size - 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }
        }

        // 3. ACTION BUTTONS: Take Photo & Gallery
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Take Photo
            Surface(
                onClick = {
                    val uri = TryOnManager.createTempCameraUri(context)
                    if (uri != null) {
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        Toast.makeText(context, "Unable to launch camera", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(16.dp),
                color = Charcoal,
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take Photo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "Take Photo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Try on any outfit",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Gallery
            Surface(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                color = SurfaceVariantColor,
                border = BorderStroke(1.dp, WarmGray),
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Charcoal,
                        modifier = Modifier.size(24.dp)
                    )
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
                        Text(
                            text = "From your gallery",
                            fontSize = 12.sp,
                            color = SoftCharcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 4. TRY TRENDING LOOKS
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Try Trending Looks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToDiscover() }
                ) {
                    Text("View All", fontSize = 13.sp, color = DeepForest, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = DeepForest,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trendingLooks, key = { it.id }) { item ->
                    TrendingLookCard(
                        data = item,
                        isFavourite = OnMeStyleRepository.isFavourite(item.id),
                        onToggleFavourite = {
                            OnMeStyleRepository.toggleFavourite(
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

@Composable
private fun TrendingLookCard(
    data: TrendingLook,
    isFavourite: Boolean,
    onToggleFavourite: () -> Unit,
    onTryOn: () -> Unit
) {
    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }
    val formattedPrice = formatter.format(data.price)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.width(156.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // [ PRODUCT IMAGE ]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
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
                // Minimum 48dp touch target with subtle circular press feedback
                Surface(
                    onClick = onToggleFavourite,
                    shape = CircleShape,
                    color = SurfaceColor.copy(alpha = 0.90f),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavourite) "Remove from favourites" else "Save to favourites",
                            tint = if (isFavourite) DeepForest else PrimaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Optional social proof: bottom-right inside the product image
                // Show tryOnCount ONLY when real backend data exists; if null, hide completely
                if (!data.tryOnCount.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
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
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                // 1. Brand and price on the same horizontal row (brand left, price right)
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
                            color = SecondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = formattedPrice,
                        fontSize = 13.sp,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // 2. Product Name: maximum 1-2 lines, ellipsis if necessary
                Text(
                    text = data.title,
                    fontSize = 12.sp,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Try On: compact visual button, minimum 48dp actual touch target
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onTryOn,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepForest,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
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
