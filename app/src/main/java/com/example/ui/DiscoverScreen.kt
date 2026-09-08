package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class DiscoverProduct(
    val id: String,
    val name: String,
    val merchant: String,
    val imageUrl: String,
    val price: Double,
    val cardHeight: Int,
    val isFavourite: Boolean = false
)

val MOCK_DISCOVER_PRODUCTS = listOf(
    DiscoverProduct(
        id = "1",
        name = "Oversized Cashmere Trench",
        merchant = "ZARA",
        imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80",
        price = 12999.0,
        cardHeight = 260
    ),
    DiscoverProduct(
        id = "2",
        name = "Tailored Wool Overcoat",
        merchant = "H&M",
        imageUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=80",
        price = 8999.0,
        cardHeight = 220
    ),
    DiscoverProduct(
        id = "3",
        name = "Minimalist Linen Blazer",
        merchant = "MANGO",
        imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
        price = 6590.0,
        cardHeight = 280
    ),
    DiscoverProduct(
        id = "4",
        name = "Structured Oxford & Trousers",
        merchant = "ZARA",
        imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
        price = 4590.0,
        cardHeight = 210
    ),
    DiscoverProduct(
        id = "5",
        name = "Emerald Satin Maxi Dress",
        merchant = "URBANIC",
        imageUrl = "https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=800&q=80",
        price = 3790.0,
        cardHeight = 250
    ),
    DiscoverProduct(
        id = "6",
        name = "Pastel Co-ord Loungewear",
        merchant = "ASOS",
        imageUrl = "https://images.unsplash.com/photo-1509631179647-0177331693ae?auto=format&fit=crop&w=800&q=80",
        price = 3290.0,
        cardHeight = 230
    )
)

enum class DiscoverTab(val title: String) {
    TRENDING("Trending Now"),
    MOST_LOVED("Most Loved"),
    BEST_SELLERS("Best Sellers"),
    JUST_IN("Just In")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(DiscoverTab.TRENDING) }
    var products by remember { mutableStateOf(MOCK_DISCOVER_PRODUCTS) }
    var showAccountPrompt by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val displayProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.merchant.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    if (showAccountPrompt) {
        AlertDialog(
            onDismissRequest = { showAccountPrompt = false },
            title = {
                Text("Save your style", fontWeight = FontWeight.Bold, color = Charcoal)
            },
            text = {
                Text("Create an account to keep your looks and track products.", color = SoftCharcoal)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccountPrompt = false
                        navController.navigate(Screen.Onboarding.route) { popUpTo(0) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Lavender, contentColor = Color.White)
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WarmIvory,
        content = { paddingValues ->
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 16.dp
            ) {
                // 1. HEADER
                item(span = StaggeredGridItemSpan.FullLine) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Discover",
                            style = Typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )

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
                                        tint = Lavender,
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

                // 2. SEARCH
                item(span = StaggeredGridItemSpan.FullLine) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        placeholder = { Text("Search outfits, brands, styles...", color = SoftCharcoal, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = SoftCharcoal)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = OffWhite,
                            unfocusedContainerColor = OffWhite,
                            unfocusedBorderColor = WarmGray,
                            focusedBorderColor = Lavender,
                            cursorColor = Lavender
                        ),
                        singleLine = true
                    )
                }

                // 3. TABS
                item(span = StaggeredGridItemSpan.FullLine) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(DiscoverTab.values()) { tab ->
                            val isSelected = selectedTab == tab
                            Surface(
                                onClick = { selectedTab = tab },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Lavender else OffWhite,
                                border = BorderStroke(1.dp, if (isSelected) Lavender else WarmGray)
                            ) {
                                Text(
                                    text = tab.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Charcoal,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // 4. PRODUCT FEED
                items(displayProducts, key = { it.id }) { product ->
                    DiscoverEditorialCard(
                        product = product,
                        onTryOn = {
                            TryOnManager.selectedProductId = product.id
                            TryOnManager.selectedProductName = product.name
                            TryOnManager.selectedProductBrand = product.merchant
                            TryOnManager.selectedProductPrice = product.price
                            TryOnManager.selectedProductImage = product.imageUrl
                            navController.navigate(Screen.TryOn.route)
                        },
                        onBuyNow = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Opening ${product.merchant} store...")
                            }
                        },
                        onToggleFavourite = {
                            if (SessionManager.isGuest) {
                                showAccountPrompt = true
                            } else {
                                val isFavedNow = !product.isFavourite
                                products = products.map {
                                    if (it.id == product.id) it.copy(isFavourite = isFavedNow) else it
                                }
                                if (isFavedNow) {
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Saved & price tracking on",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            products = products.map {
                                                if (it.id == product.id) it.copy(isFavourite = false) else it
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
    )
}

@Composable
fun DiscoverEditorialCard(
    product: DiscoverProduct,
    onTryOn: () -> Unit,
    onBuyNow: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 0
    val formattedPrice = formatter.format(product.price)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OffWhite),
        border = BorderStroke(1.dp, WarmGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(product.cardHeight.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(WarmIvory)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
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
                            imageVector = if (product.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (product.isFavourite) Lavender else Charcoal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.merchant.uppercase(),
                    fontSize = 10.sp,
                    color = SoftCharcoal,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedPrice,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onTryOn,
                    colors = ButtonDefaults.buttonColors(containerColor = Lavender, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Try On", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBuyNow() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Buy Now", fontSize = 12.sp, color = Charcoal, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Buy Now",
                        tint = Charcoal,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
