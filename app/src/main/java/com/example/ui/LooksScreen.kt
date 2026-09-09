package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

data class TryOnResult(
    val id: String,
    val userPhoto: String,
    val outfitImage: String,
    val resultImage: String,
    val createdAt: String,
    val isFavourite: Boolean,
    val productId: String,
    val productName: String,
    val productBrand: String,
    val productPrice: Double,
    val cardHeight: Int
)

data class TrackedProduct(
    val id: String,
    val productImage: String,
    val productName: String,
    val merchant: String,
    val currentPrice: Double,
    val productUrl: String,
    val trackedAt: String
)

enum class LooksTab { RECENT, FAVOURITES, PRICE_TRACKING }

val LooksTabSaver: Saver<LooksTab, String> = Saver(
    save = { it.name },
    restore = { LooksTab.valueOf(it) }
)

val MOCK_RESULTS = listOf(
    TryOnResult(
        id = "1",
        userPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
        outfitImage = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=400&q=80",
        resultImage = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80",
        createdAt = "2h ago",
        isFavourite = true,
        productId = "p1",
        productName = "Oversized Cashmere Trench",
        productBrand = "ZARA",
        productPrice = 12999.0,
        cardHeight = 250
    ),
    TryOnResult(
        id = "2",
        userPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
        outfitImage = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=400&q=80",
        resultImage = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=80",
        createdAt = "Yesterday",
        isFavourite = false,
        productId = "p2",
        productName = "Tailored Wool Overcoat",
        productBrand = "H&M",
        productPrice = 8999.0,
        cardHeight = 190
    ),
    TryOnResult(
        id = "3",
        userPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
        outfitImage = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
        resultImage = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
        createdAt = "2d ago",
        isFavourite = true,
        productId = "p3",
        productName = "Minimalist Linen Blazer",
        productBrand = "MANGO",
        productPrice = 6590.0,
        cardHeight = 230
    ),
    TryOnResult(
        id = "4",
        userPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
        outfitImage = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
        resultImage = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
        createdAt = "1w ago",
        isFavourite = false,
        productId = "p4",
        productName = "Structured Oxford & Trousers",
        productBrand = "ZARA",
        productPrice = 4590.0,
        cardHeight = 260
    )
)

val MOCK_TRACKED = listOf(
    TrackedProduct(
        id = "p1",
        productImage = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80",
        productName = "Oversized Cashmere Trench",
        merchant = "ZARA",
        currentPrice = 12999.0,
        productUrl = "",
        trackedAt = "1w ago"
    ),
    TrackedProduct(
        id = "p3",
        productImage = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
        productName = "Minimalist Linen Blazer",
        merchant = "MANGO",
        currentPrice = 6590.0,
        productUrl = "",
        trackedAt = "2w ago"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LooksScreen(
    navController: NavController,
    requestedTab: LooksTab = LooksTab.RECENT,
    onTabChanged: (LooksTab) -> Unit = {}
) {
    val saved = OnMeStyleRepository.savedResults
    val results = remember(saved, SessionManager.isGuest, OnMeStyleRepository.favouriteProductIds) {
        if (SessionManager.isGuest) emptyList()
        else {
            val combined = saved + MOCK_RESULTS.filterNot { mock ->
                saved.any { it.id == mock.id || (it.productId == mock.productId && it.resultImage == mock.resultImage) }
            }
            combined.map { item ->
                item.copy(isFavourite = OnMeStyleRepository.isFavourite(item.productId))
            }
        }
    }
    val tracked = remember(SessionManager.isGuest, OnMeStyleRepository.trackedProductIds, OnMeStyleRepository.customTrackedProducts) {
        if (SessionManager.isGuest) emptyList()
        else OnMeStyleRepository.getActiveTrackedProducts(MOCK_TRACKED)
    }
    var selectedTab by rememberSaveable(stateSaver = LooksTabSaver) { mutableStateOf(requestedTab) }

    LaunchedEffect(requestedTab) {
        selectedTab = requestedTab
    }

    val favourites = results.filter { it.isFavourite }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory),
        contentPadding = PaddingValues(bottom = SpacingXl)
    ) {
        // 1. HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Looks",
                        style = Typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your saved and generated styles",
                        style = Typography.bodyLarge,
                        color = SoftCharcoal
                    )
                }

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
        }

        // 2. TABS
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    TabPill(
                        title = "Recent",
                        isSelected = selectedTab == LooksTab.RECENT,
                        onClick = {
                            selectedTab = LooksTab.RECENT
                            onTabChanged(LooksTab.RECENT)
                        }
                    )
                }
                item {
                    TabPill(
                        title = "Favourites",
                        isSelected = selectedTab == LooksTab.FAVOURITES,
                        onClick = {
                            selectedTab = LooksTab.FAVOURITES
                            onTabChanged(LooksTab.FAVOURITES)
                        }
                    )
                }
                item {
                    TabPill(
                        title = "Price Tracking",
                        isSelected = selectedTab == LooksTab.PRICE_TRACKING,
                        onClick = {
                            selectedTab = LooksTab.PRICE_TRACKING
                            onTabChanged(LooksTab.PRICE_TRACKING)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. CONTENT
        when (selectedTab) {
            LooksTab.RECENT -> {
                item {
                    if (results.isEmpty()) {
                        EmptyLooksPlaceholder(
                            title = "No looks created yet",
                            subtitle = "Try on any outfit to generate your personalized look."
                        )
                    } else {
                        MasonryGrid(
                            items = results,
                            onItemClick = { item ->
                                TryOnManager.selectedProductId = item.productId
                                TryOnManager.selectedProductName = item.productName
                                TryOnManager.selectedProductBrand = item.productBrand
                                TryOnManager.selectedProductPrice = item.productPrice
                                TryOnManager.selectedProductImage = item.outfitImage
                                TryOnManager.selectedUserPhotoUri = item.userPhoto
                                TryOnManager.generatedResultImageUri = item.resultImage
                                navController.navigate(Screen.Result.route)
                            },
                            onToggleFavourite = { result ->
                                OnMeStyleRepository.toggleFavourite(
                                    productId = result.productId,
                                    productName = result.productName,
                                    merchant = result.productBrand,
                                    price = result.productPrice,
                                    imageUrl = result.outfitImage
                                )
                            }
                        )
                    }
                }
            }
            LooksTab.FAVOURITES -> {
                item {
                    if (favourites.isEmpty()) {
                        EmptyLooksPlaceholder(
                            title = "No favourites yet",
                            subtitle = "Save the looks you love and find them organized here."
                        )
                    } else {
                        MasonryGrid(
                            items = favourites,
                            onItemClick = { item ->
                                TryOnManager.selectedProductId = item.productId
                                TryOnManager.selectedProductName = item.productName
                                TryOnManager.selectedProductBrand = item.productBrand
                                TryOnManager.selectedProductPrice = item.productPrice
                                TryOnManager.selectedProductImage = item.outfitImage
                                TryOnManager.selectedUserPhotoUri = item.userPhoto
                                TryOnManager.generatedResultImageUri = item.resultImage
                                navController.navigate(Screen.Result.route)
                            },
                            onToggleFavourite = { result ->
                                OnMeStyleRepository.toggleFavourite(
                                    productId = result.productId,
                                    productName = result.productName,
                                    merchant = result.productBrand,
                                    price = result.productPrice,
                                    imageUrl = result.outfitImage
                                )
                            }
                        )
                    }
                }
            }
            LooksTab.PRICE_TRACKING -> {
                if (tracked.isEmpty()) {
                    item {
                        EmptyLooksPlaceholder(
                            title = "No price tracked items",
                            subtitle = "Save items from Discover to receive price drop notifications."
                        )
                    }
                } else {
                    items(tracked, key = { it.id }) { product ->
                        TrackedProductCard(
                            product = product,
                            onClick = {
                                TryOnManager.selectedProductId = product.id
                                TryOnManager.selectedProductName = product.productName
                                TryOnManager.selectedProductBrand = product.merchant
                                TryOnManager.selectedProductPrice = product.currentPrice
                                TryOnManager.selectedProductImage = product.productImage
                                navController.navigate(Screen.TryOn.route)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyLooksPlaceholder(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 54.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = DeepForest,
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Charcoal
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = Typography.bodyMedium,
            color = SoftCharcoal,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun TabPill(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) DeepForest else OffWhite,
        border = BorderStroke(1.dp, if (isSelected) DeepForest else WarmGray)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else Charcoal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun MasonryGrid(
    items: List<TryOnResult>,
    onItemClick: (TryOnResult) -> Unit,
    onToggleFavourite: (TryOnResult) -> Unit
) {
    val columnCount = 2
    val columns = List(columnCount) { mutableListOf<TryOnResult>() }

    items.forEachIndexed { index, item ->
        columns[index % columnCount].add(item)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        columns.forEach { columnItems ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                columnItems.forEach { item ->
                    TryOnResultCard(
                        result = item,
                        onClick = { onItemClick(item) },
                        onToggleFavourite = { onToggleFavourite(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun TryOnResultCard(
    result: TryOnResult,
    onClick: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OffWhite),
        border = BorderStroke(1.dp, WarmGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(result.cardHeight.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(WarmIvory)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(result.resultImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Favorite Action Button
                Surface(
                    onClick = onToggleFavourite,
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .padding(10.dp)
                        .size(32.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (result.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (result.isFavourite) DeepForest else Charcoal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.productName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = result.createdAt,
                    fontSize = 12.sp,
                    color = SoftCharcoal
                )
            }
        }
    }
}

@Composable
fun TrackedProductCard(
    product: TrackedProduct,
    onClick: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 0
    val formattedPrice = formatter.format(product.currentPrice)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OffWhite),
        border = BorderStroke(1.dp, WarmGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.productImage)
                    .crossfade(true)
                    .build(),
                contentDescription = product.productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarmIvory)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.productName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.merchant,
                    fontSize = 12.sp,
                    color = SoftCharcoal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = DeepForest,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Price tracking on",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepForest
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formattedPrice,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
                Spacer(modifier = Modifier.height(16.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View Product",
                    tint = SoftCharcoal,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
