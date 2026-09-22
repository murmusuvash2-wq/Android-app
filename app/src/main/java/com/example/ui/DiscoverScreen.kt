package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import com.example.ui.motion.TiHinAnimatedHeartIcon
import com.example.ui.motion.tihinButtonPress
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.datasource.MockProductDataSource
import com.example.data.model.Product
import com.example.data.repository.ProductRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Backward-compatible typealias pointing to the canonical Product domain model.
 */
typealias DiscoverProduct = Product

/**
 * Backward-compatible reference to the canonical catalog products in MockProductDataSource.
 */
val MOCK_DISCOVER_PRODUCTS: List<Product>
    get() = MockProductDataSource.getProducts()

enum class DiscoverTab(val title: String) {
    TRENDING("Trending Now"),
    MOST_LOVED("Most Loved"),
    BEST_SELLERS("Best Sellers"),
    JUST_IN("Just In")
}

fun getProductsForTab(products: List<Product>, tab: DiscoverTab): List<Product> {
    return when (tab) {
        DiscoverTab.TRENDING -> products
        DiscoverTab.MOST_LOVED -> products.sortedWith(compareByDescending<Product> { it.isFavourite }.thenByDescending { it.reviewCount ?: 0 })
        DiscoverTab.BEST_SELLERS -> products.sortedByDescending { it.reviewCount ?: 0 }
        DiscoverTab.JUST_IN -> products.reversed()
    }
}

private data class DiscoverCategory(val label: String, val keywords: List<String>?)

private val DISCOVER_CATEGORIES = listOf(
    DiscoverCategory("New In", null),
    DiscoverCategory("Women", listOf("women", "woman", "female")),
    DiscoverCategory("Men", listOf("men", "man", "male")),
    DiscoverCategory("Tops", listOf("top", "shirt", "t-shirt", "tee", "blouse")),
    DiscoverCategory("Bottoms", listOf("jean", "trouser", "pant", "skirt", "short")),
    DiscoverCategory("Dresses", listOf("dress", "gown")),
    DiscoverCategory("Ethnic", listOf("kurta", "kurti", "saree", "ethnic", "lehenga"))
)

private fun filterProducts(products: List<Product>, query: String, category: DiscoverCategory): List<Product> {
    val searched = if (query.isBlank()) products else products.filter {
        it.name.contains(query, ignoreCase = true) ||
        it.brand.contains(query, ignoreCase = true) ||
        it.description?.contains(query, ignoreCase = true) == true
    }
    val keywords = category.keywords ?: return searched
    return searched.filter { product ->
        val haystack = product.name + " " + product.brand + " " + product.description.orEmpty()
        keywords.any { haystack.contains(it, ignoreCase = true) }
    }
}

private fun tihinScore(product: Product): Int? {
    val rating = product.rating ?: return null
    val normalized = (rating.coerceIn(0.0, 5.0) / 5.0) * 100.0
    val reviewConfidence = when (product.reviewCount ?: 0) {
        0 -> 0.0
        in 1..9 -> 0.35
        in 10..49 -> 0.65
        in 50..199 -> 0.85
        else -> 1.0
    }
    return (normalized * (0.72 + (0.28 * reviewConfidence))).roundToInt().coerceIn(0, 100)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DISCOVER_CATEGORIES.first()) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedPriceFilter by remember { mutableStateOf<String?>(null) }
    var highScoreOnly by remember { mutableStateOf(false) }
    var showAccountPrompt by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    // Local Peek & Reveal state
    var selectedProduct by remember { mutableStateOf<DiscoverProduct?>(null) }

    val gridState = rememberLazyStaggeredGridState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val shimmerBrush = rememberShimmerBrush()

    val productRepository = remember { ProductRepository.get() }
    var baseProducts by remember { mutableStateOf(productRepository.getProducts()) }

    LaunchedEffect(Unit) {
        if (baseProducts.isEmpty()) {
            isLoading = true
        }
        isError = false
        val result = productRepository.refreshCatalog()
        if (result.isSuccess) {
            baseProducts = result.getOrNull().orEmpty()
            isError = false
        } else {
            if (baseProducts.isEmpty()) {
                isError = true
            }
        }
        isLoading = false
    }

    val onRetry: () -> Unit = {
        isLoading = true
        isError = false
        scope.launch {
            val result = productRepository.refreshCatalog()
            if (result.isSuccess) {
                baseProducts = result.getOrNull().orEmpty()
                isError = false
            } else {
                if (baseProducts.isEmpty()) {
                    isError = true
                }
            }
            isLoading = false
        }
    }

    // Safely reset reveal if search query or tab changes
    LaunchedEffect(searchQuery, selectedCategory) {
        selectedProduct = null
    }

    // Android back dismisses Peek & Reveal before leaving Discover
    BackHandler(enabled = selectedProduct != null) {
        selectedProduct = null
    }

    val products = remember(baseProducts, TiHinStyleRepository.favouriteProductIds) {
        baseProducts.map { product ->
            product.copy(isFavourite = TiHinStyleRepository.isFavourite(product.id))
        }
    }

    val displayProducts = remember(products, selectedCategory, searchQuery, selectedPriceFilter, highScoreOnly) {
        filterProducts(products, searchQuery, selectedCategory).filter { product ->
            val priceMatches = when (selectedPriceFilter) {
                "under1k" -> product.price < 1000.0
                "1to2k" -> product.price in 1000.0..1999.0
                "2kplus" -> product.price >= 2000.0
                else -> true
            }
            val scoreMatches = !highScoreOnly || (tihinScore(product) ?: 0) >= 80
            priceMatches && scoreMatches
        }
    }

    if (showAccountPrompt) {
        AlertDialog(
            onDismissRequest = { showAccountPrompt = false },
            title = {
                Text("Save your style", fontWeight = FontWeight.Bold, color = PrimaryText)
            },
            text = {
                Text("Create an account to keep your looks and track products.", color = SecondaryText)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccountPrompt = false
                        navController.navigate(Screen.Onboarding.route) { popUpTo(0) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryText, contentColor = Color.White)
                ) {
                    Text("Create Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountPrompt = false }) {
                    Text("Continue as Guest", color = PrimaryText)
                }
            },
            containerColor = SurfaceColor,
            titleContentColor = PrimaryText,
            textContentColor = SecondaryText
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
             .background(WarmIvory)
    ) {
        // 2-Column Editorial Grid
        LazyVerticalStaggeredGrid(
                    state = gridState,
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = SpacingXl, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 16.dp
                ) {
                    // 1. COMPACT DISCOVER HEADER
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discover", fontFamily = EditorialSerif, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                            if (!SessionManager.isGuest) {
                                Surface(
                                    onClick = { navController.navigate(Screen.TriesCredits.route) },
                                    shape = RoundedCornerShape(18.dp),
                                    color = SurfaceColor,
                                    border = BorderStroke(1.dp, CardBorder)
                                ) {
                                    Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, null, tint = ChampagneGold, modifier = Modifier.size(13.dp))
                                        Spacer(Modifier.width(5.dp))
                                        Text(SessionManager.credits.toString() + " Credits", fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Charcoal)
                                    }
                                }
                            }
                        }
                    }

                    // 2. SEARCH
                    item(span = StaggeredGridItemSpan.FullLine) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            placeholder = { Text("Search clothes, brands...", color = SecondaryText, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = SecondaryText, modifier = Modifier.size(19.dp)) },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceColor,
                                unfocusedContainerColor = SurfaceColor,
                                unfocusedBorderColor = CardBorder,
                                focusedBorderColor = DeepForest,
                                cursorColor = DeepForest
                            ),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontFamily = Inter)
                        )
                    }

                    // 3. CLOTHING CATEGORIES
                    item(span = StaggeredGridItemSpan.FullLine) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 6.dp)
                        ) {
                            lazyRowItems(DISCOVER_CATEGORIES) { category ->
                                val selected = selectedCategory.label == category.label
                                Surface(
                                    onClick = { selectedCategory = category },
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (selected) DeepForest else SurfaceColor,
                                    border = if (selected) null else BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Box(Modifier.padding(horizontal = 13.dp), contentAlignment = Alignment.Center) {
                                        Text(category.label, fontFamily = Inter, fontSize = 11.5.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, color = if (selected) Color.White else Charcoal)
                                    }
                                }
                            }
                        }
                    }

                    // 4. FILTER
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (searchQuery.isBlank()) selectedCategory.label + " styles" else "Search results",
                                fontFamily = EditorialSerif,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                            OutlinedButton(
                                onClick = { showFilters = !showFilters },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 5.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp), tint = DeepForest)
                                Spacer(Modifier.width(5.dp))
                                Text("Filter", fontFamily = Inter, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Charcoal)
                            }
                        }
                    }

                    if (showFilters) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SurfaceColor,
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.padding(bottom = 10.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Price", fontFamily = Inter, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Charcoal)
                                    FilterChip(
                                        selected = selectedPriceFilter == "under1k",
                                        onClick = { selectedPriceFilter = if (selectedPriceFilter == "under1k") null else "under1k" },
                                        label = { Text("< ₹1K", fontFamily = Inter, fontSize = 10.5.sp) }
                                    )
                                    FilterChip(
                                        selected = selectedPriceFilter == "1to2k",
                                        onClick = { selectedPriceFilter = if (selectedPriceFilter == "1to2k") null else "1to2k" },
                                        label = { Text("₹1K–2K", fontFamily = Inter, fontSize = 10.5.sp) }
                                    )
                                    FilterChip(
                                        selected = selectedPriceFilter == "2kplus",
                                        onClick = { selectedPriceFilter = if (selectedPriceFilter == "2kplus") null else "2kplus" },
                                        label = { Text("₹2K+", fontFamily = Inter, fontSize = 10.5.sp) }
                                    )
                                    FilterChip(
                                        selected = highScoreOnly,
                                        onClick = { highScoreOnly = !highScoreOnly },
                                        label = { Text("TiHin 80+", fontFamily = Inter, fontSize = 10.5.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // 4. PRODUCT FEED OR SKELETON / ERROR / EMPTY STATES
                    if (isLoading) {
                        items(SKELETON_CARD_HEIGHTS) { height ->
                            DiscoverSkeletonCard(
                                imageHeight = height,
                                shimmerBrush = shimmerBrush
                            )
                        }
                    } else if (isError) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            DiscoverErrorState(onRetry = onRetry)
                        }
                    } else if (displayProducts.isEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            DiscoverEmptyState(
                                isSearchEmpty = searchQuery.isNotBlank(),
                                onClearSearch = {
                                    searchQuery = ""
                                }
                            )
                        }
                    } else {
                        items(displayProducts, key = { it.id }) { product ->
                            DiscoverEditorialCard(
                                product = product,
                                onCardClick = {
                                    selectedProduct = product
                                },
                                onTryOn = {
                                    TryOnManager.selectedProductId = product.id
                                    TryOnManager.selectedProductName = product.name
                                    TryOnManager.selectedProductBrand = product.merchant
                                    TryOnManager.selectedProductPrice = product.price
                                    TryOnManager.selectedProductImage = product.imageUrl
                                    navController.navigate(Screen.TryOn.route)
                                },
                                onBuy = {
                                    val url = product.merchantUrl
                                    if (!url.isNullOrBlank()) {
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        } catch (e: Exception) {
                                            scope.launch { snackbarHostState.showSnackbar("Could not open the store link.") }
                                        }
                                    } else {
                                        scope.launch { snackbarHostState.showSnackbar("This product link isn't available yet.") }
                                    }
                                },
                                onToggleFavourite = {
                                    if (SessionManager.isGuest) {
                                        showAccountPrompt = true
                                    } else {
                                        val isFavedNow = TiHinStyleRepository.toggleFavourite(
                                            productId = product.id,
                                            productName = product.name,
                                            merchant = product.merchant,
                                            price = product.price,
                                            imageUrl = product.imageUrl
                                        )
                                        if (selectedProduct?.id == product.id) {
                                            selectedProduct = selectedProduct?.copy(isFavourite = isFavedNow)
                                        }
                                        if (isFavedNow) {
                                            scope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Saved to favourites",
                                                    actionLabel = "Undo",
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    TiHinStyleRepository.setFavourite(
                                                        productId = product.id,
                                                        isFav = false
                                                    )
                                                    if (selectedProduct?.id == product.id) {
                                                        selectedProduct = selectedProduct?.copy(isFavourite = false)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // Subtle Dimmed Scrim Over Surrounding Grid (300ms fade-in, 250ms fade-out)
                AnimatedVisibility(
                    visible = selectedProduct != null,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 250))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedProduct = null
                            }
                    )
                }

                // Peek & Reveal Expanded Product Card
                AnimatedVisibility(
                    visible = selectedProduct != null,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                            scaleIn(initialScale = 0.90f, animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 250)) +
                           scaleOut(targetScale = 0.92f, animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    selectedProduct?.let { product ->
                        PeekRevealCard(
                            product = product,
                            onClose = { selectedProduct = null },
                            onTryOn = {
                                TryOnManager.selectedProductId = product.id
                                TryOnManager.selectedProductName = product.name
                                TryOnManager.selectedProductBrand = product.merchant
                                TryOnManager.selectedProductPrice = product.price
                                TryOnManager.selectedProductImage = product.imageUrl
                                navController.navigate(Screen.TryOn.route)
                            },
                            onBuy = {
                                val url = product.merchantUrl
                                if (!url.isNullOrBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Could not open the store link.")
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("This product link isn't available yet.")
                                    }
                                }
                            },
                            onToggleFavourite = {
                                if (SessionManager.isGuest) {
                                    showAccountPrompt = true
                                } else {
                                    val isFavedNow = TiHinStyleRepository.toggleFavourite(
                                        productId = product.id,
                                        productName = product.name,
                                        merchant = product.merchant,
                                        price = product.price,
                                        imageUrl = product.imageUrl
                                    )
                                    selectedProduct = product.copy(isFavourite = isFavedNow)
                                    if (isFavedNow) {
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Saved to favourites",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                TiHinStyleRepository.setFavourite(
                                                    productId = product.id,
                                                    isFav = false
                                                )
                                                if (selectedProduct?.id == product.id) {
                                                    selectedProduct = selectedProduct?.copy(isFavourite = false)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

val SKELETON_CARD_HEIGHTS = listOf(260.dp, 220.dp, 280.dp, 210.dp, 250.dp, 230.dp)

@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "DiscoverSkeletonShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        SurfaceVariantColor, // #F3F1ED
        BackgroundColor,     // #FAF9F6
        SurfaceVariantColor  // #F3F1ED
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 450f, translateAnim - 450f),
        end = Offset(translateAnim, translateAnim)
    )
}

/**
 * Skeleton Card matching the real DiscoverEditorialCard geometry:
 * - Product image area matching staggered grid item height
 * - Circular heart button placeholder
 * - Brand tag placeholder
 * - Product name placeholder
 * - Price placeholder
 * - Try On button placeholder
 * - Completely text-free, neutral shimmer (#F3F1ED -> #FAF9F6 -> #F3F1ED)
 */
@Composable
fun DiscoverSkeletonCard(
    imageHeight: Dp,
    shimmerBrush: Brush
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = SubtleCardElevation),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Product Image Skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(shimmerBrush)
            ) {
                // Heart placeholder in top-end
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor.copy(alpha = 0.85f))
                        .align(Alignment.TopEnd)
                )
            }

            // 2. Metadata Skeleton
            Column(modifier = Modifier.padding(12.dp)) {
                // Brand tag skeleton
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Product name skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Price skeleton
                Box(
                    modifier = Modifier
                        .width(55.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Try On button skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(shimmerBrush)
                )
            }
        }
    }
}

/**
 * Compact editorial empty search state:
 * - Visually calm with soft line icon
 * - "No products found" / "Try another search or clear your search."
 * - Primary "Clear search" recovery action
 */
@Composable
fun DiscoverEmptyState(
    isSearchEmpty: Boolean,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(SurfaceVariantColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = SecondaryText,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSearchEmpty) "No products found" else "No products available",
            fontSize = 17.sp,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSearchEmpty) {
                "Try another search or clear your search."
            } else {
                "Check back soon for new arrivals in this collection."
            },
            fontSize = 13.sp,
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            color = SecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        if (isSearchEmpty) {
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onClearSearch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryText,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    text = "Clear search",
                    fontSize = 14.sp,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Compact editorial error state:
 * - "Couldn't load products" / "Please check your connection and try again."
 * - Primary "Try again" recovery button
 */
@Composable
fun DiscoverErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(SurfaceVariantColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Connection error",
                tint = SecondaryText,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Couldn't load products",
            fontSize = 17.sp,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Please check your connection and try again.",
            fontSize = 13.sp,
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            color = SecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepForest,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            modifier = Modifier.height(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Try again",
                fontSize = 14.sp,
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * Normal Grid Card:
 * - Product/model image
 * - Heart/favourite control (separate interaction, does not trigger card click)
 * - Brand tag
 * - Product name
 * - ₹ Price
 * - Primary Try On button
 * - Card is tappable to open Peek & Reveal
 * - Buy Now is NOT present on the normal grid card
 */
@Composable
fun DiscoverEditorialCard(
    product: DiscoverProduct,
    onCardClick: () -> Unit,
    onTryOn: () -> Unit,
    onBuy: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 } }
    val formattedPrice = formatter.format(product.price)
    val score = tihinScore(product)
    val imageHeight = product.cardHeight.coerceIn(190, 280).dp

    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onCardClick)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(imageHeight).clip(RoundedCornerShape(14.dp)).background(SurfaceVariantColor)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(product.imageUrl).crossfade(220).build(),
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                onClick = onToggleFavourite,
                shape = CircleShape,
                color = SurfaceColor.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.padding(8.dp).minimumInteractiveComponentSize().size(34.dp).align(Alignment.TopEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    TiHinAnimatedHeartIcon(
                        isFavourite = product.isFavourite,
                        contentDescription = if (product.isFavourite) "Remove from favourites" else "Save product",
                        activeTint = DeepForest,
                        inactiveTint = PrimaryText,
                        iconSize = 17.dp
                    )
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(product.merchant, fontFamily = Inter, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, color = SecondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formattedPrice, fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            if (score != null) Text("TiHin $score", fontFamily = Inter, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = DeepForest)
        }
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val interaction = remember { MutableInteractionSource() }
            Button(
                onClick = onTryOn,
                interactionSource = interaction,
                colors = ButtonDefaults.buttonColors(containerColor = DeepForest, contentColor = Color.White),
                shape = RoundedCornerShape(9.dp),
                contentPadding = PaddingValues(horizontal = 7.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp).weight(0.52f).tihinButtonPress(interaction)
            ) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Try On", fontFamily = Inter, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
            }
            if (!product.merchantUrl.isNullOrBlank()) {
                OutlinedButton(
                    onClick = onBuy,
                    shape = RoundedCornerShape(9.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).weight(0.48f)
                ) {
                    Text("Buy on " + product.merchant, fontFamily = Inter, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/**
 * Peek & Reveal In-Place Expansion (STEP 2B: Product Gallery):
 * - Merchant product image gallery with auto-rotation (3-4s) and manual swipe
 * - Pinch-to-zoom support (1x to 3x)
 * - Compact image counter overlay (e.g. 1/3)
 * - Clearly visible close button (primary dismissal) & heart toggle
 * - Brand, product name, price, rating, concise description, and Style Tip
 * - Primary action: Try On (launches existing universal Try-On flow)
 * - Secondary action: Buy ↗
 * - Respects the locked STEP 1 design system
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PeekRevealCard(
    product: DiscoverProduct,
    onClose: () -> Unit,
    onTryOn: () -> Unit,
    onBuy: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 } }
    val formattedPrice = formatter.format(product.price)
    val score = tihinScore(product)
    val images = remember(product) { if (product.productImages.isNotEmpty()) product.productImages else listOf(product.imageUrl) }
    val pagerState = rememberPagerState(pageCount = { images.size })

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth().widthIn(max = 380.dp).fillMaxHeight(0.78f).padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).background(SurfaceVariantColor)
            ) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(images[page]).crossfade(true).build(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = onClose,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.minimumInteractiveComponentSize().size(38.dp)
                    ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, "Close", tint = PrimaryText, modifier = Modifier.size(18.dp)) } }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = { TiHinStyleRepository.togglePriceTracking(product.id) },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.94f),
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.minimumInteractiveComponentSize().size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(if (TiHinStyleRepository.isPriceTracked(product.id)) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone, "Price tracking", tint = if (TiHinStyleRepository.isPriceTracked(product.id)) ChampagneGold else PrimaryText, modifier = Modifier.size(18.dp))
                            }
                        }
                        Surface(
                            onClick = onToggleFavourite,
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.94f),
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.minimumInteractiveComponentSize().size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                TiHinAnimatedHeartIcon(
                                isFavourite = product.isFavourite,
                                contentDescription = if (product.isFavourite) "Remove from favourites" else "Save product",
                                activeTint = DeepForest,
                                inactiveTint = PrimaryText,
                                iconSize = 18.dp
                            )
                            }
                        }
                    }
                }
                if (images.size > 1) {
                    Surface(shape = RoundedCornerShape(12.dp), color = PrimaryText.copy(alpha = 0.72f), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)) {
                        Text(
                            (pagerState.currentPage + 1).toString() + " / " + images.size,
                            fontFamily = Inter, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(product.merchant.uppercase(), fontFamily = Inter, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.7.sp, color = SecondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text(product.name, fontFamily = Inter, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = PrimaryText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(formattedPrice, fontFamily = Inter, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    if (score != null) Text("TiHin Score " + score, fontFamily = Inter, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DeepForest)
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val tryInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = onTryOn,
                        interactionSource = tryInteraction,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepForest, contentColor = Color.White),
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier.height(42.dp).weight(0.58f).tihinButtonPress(tryInteraction)
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Try On", fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    val buyInteraction = remember { MutableInteractionSource() }
                    OutlinedButton(
                        onClick = onBuy,
                        interactionSource = buyInteraction,
                        shape = RoundedCornerShape(11.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceVariantColor, contentColor = PrimaryText),
                        modifier = Modifier.height(42.dp).weight(0.42f).tihinButtonPress(buyInteraction)
                    ) {
                        Text("Buy on " + product.merchant, fontFamily = Inter, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

