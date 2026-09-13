package com.example.ui

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

/**
 * Pure, deterministic function to filter and order catalog products according to the active Discover tab.
 *
 * Rules:
 * - TRENDING: Preserves default curated trending catalog order.
 * - MOST_LOVED: Prioritizes favourited items first (unified with TiHinStyleRepository), followed by reviewCount (customer feedback/admiration).
 * - BEST_SELLERS: Ranks by available sales/feedback volume (reviewCount descending). Products without review counts follow in natural order.
 * - JUST_IN: Real new arrival ordering (reverses initial catalogue index so newest entries appear first).
 */
fun getProductsForTab(products: List<Product>, tab: DiscoverTab): List<Product> {
    return when (tab) {
        DiscoverTab.TRENDING -> products
        DiscoverTab.MOST_LOVED -> {
            // First show user-favourited items, then items with highest review count (existing real feedback data)
            products.sortedWith(
                compareByDescending<Product> { it.isFavourite }
                    .thenByDescending { it.reviewCount ?: 0 }
            )
        }
        DiscoverTab.BEST_SELLERS -> {
            // Order by customer volume/feedback (reviewCount descending, then stable natural index)
            products.sortedWith(
                compareByDescending { it.reviewCount ?: 0 }
            )
        }
        DiscoverTab.JUST_IN -> {
            // New arrivals: most recently added catalogue items first
            products.reversed()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(DiscoverTab.TRENDING) }
    var showAccountPrompt by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    // Local Peek & Reveal state
    var selectedProduct by remember { mutableStateOf<DiscoverProduct?>(null) }

    val gridState = rememberLazyStaggeredGridState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val shimmerBrush = rememberShimmerBrush()

    // Simulate initial loading transition
    LaunchedEffect(Unit) {
        delay(400)
        isLoading = false
    }

    val onRetry: () -> Unit = {
        isLoading = true
        isError = false
        scope.launch {
            delay(600)
            isLoading = false
        }
    }

    // Safely reset reveal if search query or tab changes
    LaunchedEffect(searchQuery, selectedTab) {
        selectedProduct = null
    }

    // Android back dismisses Peek & Reveal before leaving Discover
    BackHandler(enabled = selectedProduct != null) {
        selectedProduct = null
    }

    val productRepository = remember { ProductRepository.get() }
    val baseProducts = remember { productRepository.getProducts() }
    val products = remember(baseProducts, TiHinStyleRepository.favouriteProductIds) {
        baseProducts.map { product ->
            product.copy(isFavourite = TiHinStyleRepository.isFavourite(product.id))
        }
    }

    val displayProducts = remember(products, selectedTab, searchQuery) {
        val tabFiltered = getProductsForTab(products, selectedTab)
        if (searchQuery.isBlank()) {
            tabFiltered
        } else {
            tabFiltered.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.merchant.contains(searchQuery, ignoreCase = true)
            }
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
            .background(BackgroundColor)
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
                    // 1. HEADER
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Discover",
                                style = Typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )

                            if (!SessionManager.isGuest) {
                                Surface(
                                    onClick = { navController.navigate(Screen.TriesCredits.route) },
                                    shape = RoundedCornerShape(20.dp),
                                    color = SurfaceColor,
                                    border = BorderStroke(1.dp, BorderColor)
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
                                            color = PrimaryText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. SEARCH
                    item(span = StaggeredGridItemSpan.FullLine) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                selectedProduct = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            placeholder = {
                                Text("Search outfits, brands, products...", color = SecondaryText, fontSize = 14.sp)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = SecondaryText)
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceVariantColor,
                                unfocusedContainerColor = SurfaceVariantColor,
                                unfocusedBorderColor = BorderColor,
                                focusedBorderColor = DeepForest,
                                cursorColor = DeepForest
                            ),
                            singleLine = true
                        )
                    }

                    // 3. TABS (Fixed single-row compact editorial tabs, all 4 visible without scrolling)
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DiscoverTab.values().forEach { tab ->
                                val isSelected = selectedTab == tab
                                Column(
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            selectedTab = tab
                                            selectedProduct = null
                                        }
                                        .padding(horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = tab.title,
                                        fontSize = 12.sp,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) DeepForest else SecondaryText,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height(2.dp)
                                            .background(
                                                color = if (isSelected) DeepForest else Color.Transparent,
                                                shape = RoundedCornerShape(1.dp)
                                            )
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
                                                    message = "Saved & price tracking on",
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
                                scope.launch {
                                    snackbarHostState.showSnackbar("Opening ${product.merchant} store...")
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
                                                message = "Saved & price tracking on",
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
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
    onToggleFavourite: () -> Unit
) {
    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }
    val formattedPrice = formatter.format(product.price)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(product.cardHeight.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(SurfaceVariantColor)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(250)
                        .build(),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Favorite Action Button - isolated touch target
                Surface(
                    onClick = onToggleFavourite,
                    shape = CircleShape,
                    color = SurfaceColor.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier
                        .padding(8.dp)
                        .minimumInteractiveComponentSize()
                        .size(36.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (product.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (product.isFavourite) "Remove from favourites" else "Add to favourites",
                            tint = if (product.isFavourite) DeepForest else PrimaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 16.dp)) {
                Text(
                    text = product.merchant.uppercase(),
                    style = BrandTagStyle,
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.name,
                    style = ProductNameStyle,
                    color = PrimaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedPrice,
                    style = PriceStyle,
                    color = PrimaryText
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onTryOn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurchaseCTA,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Try On",
                        style = ButtonTextStyle,
                        fontSize = 13.sp,
                        color = Color.White
                    )
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
    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }
    val formattedPrice = formatter.format(product.price)
    val images = remember(product) {
        if (product.productImages.isNotEmpty()) product.productImages else listOf(product.imageUrl)
    }

    val pagerState = rememberPagerState(pageCount = { images.size })

    // Track user interacting globally to disable auto-scroll
    var isUserInteracting by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState, images.size, isUserInteracting) {
        if (images.size > 1 && !isUserInteracting) {
            while (isActive) {
                delay(3500)
                if (!pagerState.isScrollInProgress && !isUserInteracting) {
                    val nextPage = (pagerState.currentPage + 1) % images.size
                    pagerState.animateScrollToPage(nextPage, animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing))
                }
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f) // Take up substantially more screen height
            .widthIn(max = 380.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Consume click inside card */ }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. PRODUCT GALLERY VISUAL AREA (Hero Image)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Expands to fill available vertical space
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(SurfaceVariantColor)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = !isUserInteracting // Lock pager when zooming
                ) { page ->
                    var scale by remember { mutableFloatStateOf(1f) }
                    var offsetX by remember { mutableFloatStateOf(0f) }
                    var offsetY by remember { mutableFloatStateOf(0f) }

                    // Reset zoom state if pager changes to another page
                    LaunchedEffect(pagerState.currentPage) {
                        if (pagerState.currentPage != page) {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                            if (isUserInteracting) isUserInteracting = false
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        if (scale > 1f) {
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                            isUserInteracting = false
                                        } else {
                                            scale = 2f
                                            isUserInteracting = true
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val newScale = (scale * zoom).coerceIn(1f, 3f)
                                    scale = newScale
                                    if (scale > 1.05f) {
                                        val maxX = (size.width * (scale - 1)) / 2f
                                        val maxY = (size.height * (scale - 1)) / 2f
                                        offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                                        offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                                        isUserInteracting = true
                                    } else {
                                        scale = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                        isUserInteracting = false
                                    }
                                }
                            }
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offsetX
                                translationY = offsetY
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(images[page])
                                .crossfade(true)
                                .build(),
                            contentDescription = "${product.name} - view ${page + 1} of ${images.size}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Top Controls: Close button (Primary dismissal) & Heart button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close Button
                    Surface(
                        onClick = onClose,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close product preview",
                                tint = PrimaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Heart Button
                    Surface(
                        onClick = onToggleFavourite,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (product.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (product.isFavourite) "Remove from favourites" else "Add to favourites",
                                tint = if (product.isFavourite) DeepForest else PrimaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Bottom Gallery Indicator / Counter Pill (shown if > 1 image)
                if (images.size > 1) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryText.copy(alpha = 0.72f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${images.size}",
                                style = MetadataCaptionStyle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 2. PRODUCT INFORMATION & ACTIONS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Scrollable metadata area to accommodate small screens / font scaling
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.merchant.uppercase(),
                            style = BrandTagStyle,
                            color = SecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Optional Rating Pill
                        if (product.rating != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = ChampagneGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f", product.rating),
                                    style = MetadataCaptionStyle,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryText
                                )
                                if (product.reviewCount != null) {
                                    Text(
                                        text = "(${product.reviewCount})",
                                        style = MetadataCaptionStyle,
                                        color = TertiaryText,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = product.name,
                        style = ProductNameStyle,
                        fontSize = 15.sp,
                        color = PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = formattedPrice,
                        style = PriceStyle,
                        fontSize = 15.sp,
                        color = PrimaryText
                    )

                    if (!product.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.description,
                            style = BodyContentStyle,
                            color = SecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. ACTIONS: Primary "Try On" (60%) and Secondary "Buy ↗" (40%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Primary Action: Try On
                    Button(
                        onClick = onTryOn,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurchaseCTA,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(0.6f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Try On",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }

                    // Secondary Action: Buy ↗
                    OutlinedButton(
                        onClick = onBuy,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SurfaceVariantColor,
                            contentColor = PrimaryText
                        ),
                        modifier = Modifier
                            .weight(0.4f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Buy ↗",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
