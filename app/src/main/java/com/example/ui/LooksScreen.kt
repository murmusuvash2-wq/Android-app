package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DeepForest
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftCharcoal
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.SurfaceVariantColor
import com.example.ui.theme.WarmIvory

@Composable
fun LooksScreen(
    navController: NavController,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    androidx.compose.runtime.LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }
    val tabs = listOf("Recent", "Favourites", "Price Tracking")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .testTag("looks_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Looks",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = WarmIvory,
            contentColor = DeepForest,
            edgePadding = 20.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) DeepForest else SecondaryText
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) togetherWith
                        fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
            },
            label = "looks_tab_content"
        ) { tab ->
            when (tab) {
                0 -> RecentLooksTab(navController)
                1 -> FavouritesTab(navController)
                2 -> PriceTrackingTab(navController)
            }
        }
    }
}

@Composable
fun RecentLooksTab(navController: NavController) {
    val looks = TiHinStyleRepository.savedLooks
    var lookToDelete by remember { mutableStateOf<String?>(null) }

    if (lookToDelete != null) {
        AlertDialog(
            onDismissRequest = { lookToDelete = null },
            title = { Text("Remove Look", color = PrimaryText) },
            text = { Text("Are you sure you want to remove this saved try-on result?", color = SecondaryText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        lookToDelete?.let { TiHinStyleRepository.deleteSavedLook(it) }
                        lookToDelete = null
                    }
                ) {
                    Text("Remove", color = DeepForest, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { lookToDelete = null }) {
                    Text("Cancel", color = SecondaryText)
                }
            },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (looks.isEmpty()) {
        EmptyLooksState(
            title = "No generated looks yet",
            subtitle = "Try on items from Home or Discover to build your personal virtual fitting catalog."
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(looks, key = { it.id }) { look ->
                LookCard(
                    look = look,
                    onTap = {
                        TryOnManager.selectedProductId = look.productId
                        TryOnManager.selectedProductName = look.productName
                        TryOnManager.selectedProductBrand = look.brand
                        TryOnManager.selectedProductPrice = look.price
                        TryOnManager.generatedResultImageUri = look.resultImageUri
                        navController.navigate(Screen.Result.route)
                    },
                    onDelete = { lookToDelete = look.id }
                )
            }
        }
    }
}

@Composable
fun FavouritesTab(navController: NavController) {
    val favIds = TiHinStyleRepository.favouriteProductIds

    if (favIds.isEmpty()) {
        EmptyLooksState(
            title = "No favourites yet",
            subtitle = "Tap the heart on items you love in Discover or Home to keep them handy."
        )
    } else {
        val favLooks = favIds.map { id -> TiHinStyleRepository.getProductLookup(id) }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(favLooks, key = { it.productId }) { look ->
                LookCard(
                    look = look,
                    onTap = {
                        TryOnManager.selectedProductId = look.productId
                        TryOnManager.selectedProductName = look.productName
                        TryOnManager.selectedProductBrand = look.brand
                        TryOnManager.selectedProductPrice = look.price
                        val saved = TiHinStyleRepository.savedLooks.firstOrNull { it.productId == look.productId }
                        if (saved != null) {
                            TryOnManager.generatedResultImageUri = saved.resultImageUri
                            navController.navigate(Screen.Result.route)
                        } else {
                            TryOnManager.selectedProductImage = look.resultImageUri
                            navController.navigate(Screen.TryOn.route)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PriceTrackingTab(navController: NavController) {
    val trackedIds = TiHinStyleRepository.priceTrackedProductIds

    if (trackedIds.isEmpty()) {
        EmptyLooksState(
            title = "No items tracked",
            subtitle = "Enable Price Drop alerts on products to track merchant price changes."
        )
    } else {
        val trackedLooks = trackedIds.map { id -> TiHinStyleRepository.getProductLookup(id) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(trackedLooks, key = { it.productId }) { look ->
                TrackedLookRow(look, navController)
            }
        }
    }
}

@Composable
fun LookCard(
    look: SavedLook,
    onTap: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(SurfaceVariantColor)
            ) {
                AsyncImage(
                    model = look.resultImageUri,
                    contentDescription = look.productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (onDelete != null) {
                    val deleteInteraction = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = onDelete,
                        interactionSource = deleteInteraction,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SurfaceColor.copy(alpha = 0.85f))
                            .tihinButtonPress(deleteInteraction, pressedScale = 0.90f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = SecondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                val favInteraction = remember { MutableInteractionSource() }
                val isFav = TiHinStyleRepository.isFavourite(look.productId)
                IconButton(
                    onClick = {
                        TiHinStyleRepository.toggleFavourite(look.productId)
                    },
                    interactionSource = favInteraction,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor.copy(alpha = 0.85f))
                        .tihinButtonPress(favInteraction, pressedScale = 0.90f)
                ) {
                    TiHinAnimatedHeartIcon(
                        isFavourite = isFav,
                        contentDescription = "Favourite",
                        activeTint = DeepForest,
                        inactiveTint = SecondaryText,
                        iconSize = 18.dp
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = look.brand.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryText
                )
                Text(
                    text = look.productName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${look.price.toInt()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            }
        }
    }
}

@Composable
fun TrackedLookRow(look: SavedLook, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                TryOnManager.selectedProductId = look.productId
                TryOnManager.selectedProductName = look.productName
                TryOnManager.selectedProductBrand = look.brand
                TryOnManager.selectedProductPrice = look.price
                val saved = TiHinStyleRepository.savedLooks.firstOrNull { it.productId == look.productId }
                if (saved != null) {
                    TryOnManager.generatedResultImageUri = saved.resultImageUri
                    navController.navigate(Screen.Result.route)
                } else {
                    TryOnManager.selectedProductImage = look.resultImageUri
                    navController.navigate(Screen.TryOn.route)
                }
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariantColor)
            ) {
                AsyncImage(
                    model = look.resultImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = look.brand.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryText
                )
                Text(
                    text = look.productName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "₹${look.price.toInt()} · Price alert active",
                    fontSize = 12.sp,
                    color = DeepForest,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = { TiHinStyleRepository.togglePriceTracking(look.productId) }) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Price active",
                    tint = ChampagneGold
                )
            }
        }
    }
}

@Composable
fun EmptyLooksState(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = SecondaryText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
