package com.example.ui

import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Product
import com.example.data.repository.ProductRepository
import com.example.ui.motion.TiHinAnimatedCreditsPill
import com.example.ui.motion.TiHinAnimatedHeartIcon
import com.example.ui.motion.tihinButtonPress
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToDiscover: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val productRepo = remember { ProductRepository.get() }
    var catalogVersion by remember { mutableIntStateOf(0) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        val result = productRepo.refreshCatalog()
        if (result.isSuccess) catalogVersion++
    }

    val products = remember(catalogVersion) { productRepo.getProducts() }
    val trendingProducts = remember(catalogVersion) {
        productRepo.getTrendingProducts().ifEmpty {
            products.sortedByDescending { it.trendingScore ?: 0.0 }
        }.take(10)
    }
    val mostLovedProducts = remember(catalogVersion) {
        products.sortedWith(
            compareByDescending<Product> { it.loveCount ?: 0 }
                .thenByDescending { it.salesCount ?: 0 }
                .thenByDescending { it.rating ?: 0.0 }
        ).take(8)
    }
    val heroProduct = trendingProducts.firstOrNull() ?: products.firstOrNull()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                TryOnManager.updateUserPhoto(tempCameraUri.toString(), context)
                selectHomeProductIfNeeded(heroProduct)
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
                selectHomeProductIfNeeded(heroProduct)
                navController.navigate(Screen.TryOn.route)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp, bottom = 24.dp)
    ) {
        HomeHeader(navController)
        Spacer(Modifier.height(10.dp))

        HeroTryOnCard(
            product = heroProduct,
            onTryOn = {
                if (heroProduct != null) {
                    selectHomeProduct(heroProduct)
                    navController.navigate(Screen.TryOn.route)
                }
            }
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomePhotoAction(
                title = "Take Photo",
                subtitle = "Use your camera",
                icon = { Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f),
                primary = true,
                onClick = {
                    val uri = TryOnManager.createTempCameraUri(context)
                    if (uri != null) {
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    }
                }
            )
            HomePhotoAction(
                title = "Choose Photo",
                subtitle = "From your gallery",
                icon = { Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f),
                primary = false,
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }

        Spacer(Modifier.height(18.dp))

        HomeSectionHeader(
            title = "Explore styles",
            subtitle = "Clothing for every mood",
            action = "Discover",
            onAction = onNavigateToDiscover
        )
        Spacer(Modifier.height(10.dp))
        CategoryRow(products = products, onDiscover = onNavigateToDiscover)

        Spacer(Modifier.height(22.dp))

        HomeSectionHeader(
            title = "Trending Now",
            subtitle = "Looks people are trying right now.",
            action = "See all",
            onAction = onNavigateToDiscover
        )
        Spacer(Modifier.height(10.dp))
        TrendingEditorial(
            products = trendingProducts.take(3),
            onTryOn = { product ->
                selectHomeProduct(product)
                navController.navigate(Screen.TryOn.route)
            },
            onDiscover = onNavigateToDiscover
        )

        Spacer(Modifier.height(22.dp))

        HomeSectionHeader(
            title = "Most Loved",
            subtitle = "Popular pieces from the TiHin catalog.",
            action = "See all",
            onAction = onNavigateToDiscover
        )
        Spacer(Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(mostLovedProducts, key = { it.id }) { product ->
                HomeProductCard(
                    product = product,
                    onTryOn = {
                        selectHomeProduct(product)
                        navController.navigate(Screen.TryOn.route)
                    }
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        BrandBanner()
        Spacer(Modifier.height(8.dp))
    }
}

private fun selectHomeProductIfNeeded(product: Product?) {
    if (TryOnManager.selectedProductId == null && product != null) {
        selectHomeProduct(product)
    }
}

private fun selectHomeProduct(product: Product) {
    TryOnManager.selectedProductId = product.id
    TryOnManager.selectedProductName = product.name
    TryOnManager.selectedProductBrand = product.brand
    TryOnManager.selectedProductPrice = product.price
    TryOnManager.selectedProductImage = product.primaryImageUrl
}

@Composable
private fun HomeHeader(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            TiHinLogo(textSize = 27.sp, showSparkle = true)
            Text(
                text = "Try • Love • Wear",
                fontFamily = Inter,
                fontSize = 11.sp,
                color = SoftCharcoal,
                letterSpacing = 0.7.sp,
                modifier = Modifier.offset(y = (-2).dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!SessionManager.isGuest) {
                TiHinAnimatedCreditsPill(
                    credits = SessionManager.credits,
                    onClick = { navController.navigate(Screen.TriesCredits.route) }
                )
            }

            val source = remember { MutableInteractionSource() }
            Surface(
                onClick = { },
                shape = CircleShape,
                color = SurfaceColor,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                     .size(38.dp)
                    .tihinButtonPress(source, pressedScale = 0.92f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = Charcoal,
                        modifier = Modifier.size(18.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .size(6.dp)
                            .background(DeepForest, CircleShape)
                            .border(1.dp, SurfaceColor, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroTryOnCard(product: Product?, onTryOn: () -> Unit) {
    val source = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
             .height(230.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceVariantColor)
    ) {
        if (product != null && product.primaryImageUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.primaryImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Charcoal.copy(alpha = 0.80f),
                                Charcoal.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp, end = 128.dp)
        ) {
            Text(
                text = "YOUR STYLE,",
                fontFamily = EditorialSerif,
                fontSize = 26.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "YOURSELF",
                fontFamily = EditorialSerif,
                fontSize = 26.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Try on real outfits before you buy.",
                fontFamily = Inter,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = Color.White.copy(alpha = 0.92f)
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onTryOn,
                interactionSource = source,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = DeepForest
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 13.dp, vertical = 7.dp),
                modifier = Modifier.tihinButtonPress(source).height(38.dp)
            ) {
                Text(
                    text = "Try On Now",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(5.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp))
            }
        }

        if (product != null) {
            Surface(
                shape = RoundedCornerShape(9.dp),
                color = Color.Black.copy(alpha = 0.48f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
            ) {
                Text(
                    text = product.name,
                    color = Color.White,
                    fontFamily = Inter,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun HomePhotoAction(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    modifier: Modifier,
    primary: Boolean,
    onClick: () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = source,
        shape = RoundedCornerShape(16.dp),
        color = if (primary) DeepForest else SurfaceColor,
        border = if (primary) null else BorderStroke(1.dp, CardBorder),
        shadowElevation = if (primary) 1.dp else 0.dp,
        modifier = modifier
            .height(62.dp)
            .tihinButtonPress(source)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                     .size(34.dp)
                    .background(
                        if (primary) Color.White.copy(alpha = 0.14f) else DeepForestContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides if (primary) Color.White else DeepForest
                ) {
                    icon()
                }
            }
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (primary) Color.White else Charcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    fontFamily = Inter,
                    fontSize = 9.5.sp,
                    color = if (primary) Color.White.copy(alpha = 0.78f) else SoftCharcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (primary) Color.White else DeepForest,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun CategoryRow(products: List<Product>, onDiscover: () -> Unit) {
    val categories = listOf(
        "New In" to null,
        "Women" to listOf("women", "woman", "female"),
        "Men" to listOf("men", "man", "male"),
        "Tops" to listOf("top", "shirt", "t-shirt", "tee", "blouse"),
        "Bottoms" to listOf("jean", "trouser", "pant", "skirt", "short"),
        "Dresses" to listOf("dress", "gown"),
        "Ethnic" to listOf("kurta", "kurti", "saree", "ethnic", "lehenga")
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(end = 4.dp)
    ) {
        items(categories) { category ->
            val label = category.first
            val keywords = category.second
            val sample = if (keywords == null) {
                products.firstOrNull()
            } else {
                products.firstOrNull { item ->
                    val haystack = item.name + " " + item.description.orEmpty()
                    keywords.any { haystack.contains(it, ignoreCase = true) }
                }
            }
            CategoryChip(label, sample?.primaryImageUrl, onDiscover)
        }
    }
}

@Composable
private fun CategoryChip(label: String, imageUrl: String?, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
             .width(64.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                 .size(54.dp)
                .clip(CircleShape)
                .background(SurfaceColor)
                .border(1.dp, CardBorder, CircleShape)
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = label,
                    tint = DeepForest,
                    modifier = Modifier.align(Alignment.Center) .size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            fontFamily = Inter,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Charcoal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TrendingEditorial(
    products: List<Product>,
    onTryOn: (Product) -> Unit,
    onDiscover: () -> Unit
) {
    if (products.isEmpty()) return

    val feature = products.first()
    val sideProducts = products.drop(1).take(2)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        EditorialFeatureCard(
            product = feature,
            modifier = Modifier.weight(1.08f).height(250.dp),
            onTryOn = { onTryOn(feature) }
        )

        Column(
            modifier = Modifier.weight(0.92f).height(250.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            sideProducts.forEach { product ->
                EditorialSmallCard(
                    product = product,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onTryOn = { onTryOn(product) }
                )
            }
            if (sideProducts.size < 2) {
                EditorialSmallPlaceholder(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = onDiscover
                )
            }
        }
    }
}

@Composable
private fun EditorialFeatureCard(
    product: Product,
    modifier: Modifier,
    onTryOn: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceVariantColor)
            .clickable(onClick = onTryOn)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(product.primaryImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Transparent, Charcoal.copy(alpha = 0.78f))
                    )
                )
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)
        ) {
            Text(
                text = "NEW LOOK",
                fontFamily = Inter,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.3.sp,
                color = Color.White.copy(alpha = 0.82f)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = product.name,
                fontFamily = EditorialSerif,
                fontSize = 20.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Try this look  →",
                fontFamily = Inter,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EditorialSmallCard(
    product: Product,
    modifier: Modifier,
    onTryOn: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceVariantColor)
            .clickable(onClick = onTryOn)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(product.primaryImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Charcoal.copy(alpha = 0.66f), Color.Transparent)
                    )
                )
        )
        Text(
            text = product.name,
            fontFamily = EditorialSerif,
            fontSize = 17.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EditorialSmallPlaceholder(modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = DeepForestContainer,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "More styles",
                fontFamily = EditorialSerif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DeepForest
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "Explore the full collection →",
                fontFamily = Inter,
                fontSize = 11.sp,
                color = DeepForest
            )
        }
    }
}

@Composable
private fun HomeProductCard(product: Product, onTryOn: () -> Unit) {
    val configuration = LocalConfiguration.current
    val cardWidth = (configuration.screenWidthDp.dp * 0.48f).coerceIn(165.dp, 196.dp)
    val isFavourite = TiHinStyleRepository.isFavourite(product.id)
    val priceFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = 0
        }
    }
    val heartSource = remember { MutableInteractionSource() }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.width(cardWidth)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                     .height(170.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(SurfaceVariantColor)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.primaryImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    onClick = {
                        TiHinStyleRepository.toggleFavourite(
                            productId = product.id,
                            productName = product.name,
                            merchant = product.brand,
                            price = product.price,
                            imageUrl = product.primaryImageUrl
                        )
                    },
                    interactionSource = heartSource,
                    shape = CircleShape,
                    color = SurfaceColor.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(9.dp)
                        .size(34.dp)
                        .tihinButtonPress(heartSource, pressedScale = 0.90f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        TiHinAnimatedHeartIcon(
                            isFavourite = isFavourite,
                            contentDescription = "Favourite",
                            activeTint = DeepForest,
                            inactiveTint = Charcoal,
                            iconSize = 17.dp
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 11.dp, vertical = 10.dp)) {
                Text(
                    text = product.brand.ifBlank { "TiHin Edit" }.uppercase(),
                    fontFamily = Inter,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.9.sp,
                    color = DeepForest,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = product.name,
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Charcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = priceFormatter.format(product.price),
                    fontFamily = Inter,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
                Spacer(Modifier.height(9.dp))
                val trySource = remember { MutableInteractionSource() }
                OutlinedButton(
                    onClick = onTryOn,
                    interactionSource = trySource,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DeepForest.copy(alpha = 0.55f)),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp)
                        .tihinButtonPress(trySource)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = DeepForest,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Try On",
                        fontFamily = Inter,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepForest
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    subtitle: String,
    action: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = EditorialSerif,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                letterSpacing = (-0.3).sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = Inter,
                fontSize = 11.5.sp,
                color = SoftCharcoal
            )
        }
        Text(
            text = action,
            fontFamily = Inter,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = DeepForest,
            modifier = Modifier
                .clickable(onClick = onAction)
                .padding(start = 10.dp, bottom = 2.dp)
        )
    }
}

@Composable
private fun BrandBanner() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DeepForestContainer,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(SurfaceColor.copy(alpha = 0.78f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✦",
                    fontFamily = EditorialSerif,
                    fontSize = 20.sp,
                    color = ChampagneGold
                )
            }
            Spacer(Modifier.width(13.dp))
            Column {
                Text(
                    text = "Fashion that fits your life.",
                    fontFamily = EditorialSerif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepForest
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Try. Love. Wear. TiHin.",
                    fontFamily = Inter,
                    fontSize = 11.5.sp,
                    color = DeepForest.copy(alpha = 0.78f)
                )
            }
        }
    }
}
