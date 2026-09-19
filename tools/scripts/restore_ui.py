import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

# The point of failure is at:
#     if (showAccountPrompt) {
#         AlertDialog(
#             onDismissRequest = { showAccountPrompt = false },

# We will cut everything from confirmButton in AlertDialog down to PeekRevealCard.
pattern = r"confirmButton = \{[\s\S]*?\}\n\s*\}\n\s*\}\n\s*\}\n\s*/\*\*\n\s*\* Peek & Reveal"

replacement = """confirmButton = {
                Button(onClick = { showAccountPrompt = false }, colors = ButtonDefaults.buttonColors(containerColor = DeepForest, contentColor = Color.White)) {
                    Text("Got it")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            state = gridState,
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 16.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. HEADER (GlobalCreditPill and Title)
            item(span = StaggeredGridItemSpan.FullLine) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Discover",
                        style = Typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    GlobalCreditPill()
                }
            }

            // 2. SEARCH BAR
            item(span = StaggeredGridItemSpan.FullLine) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by brand, style, or item...", color = SecondaryText, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryText) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceColor,
                        unfocusedContainerColor = SurfaceColor,
                        focusedBorderColor = DeepForest,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }

            // 3. TABS + FILTER
            item(span = StaggeredGridItemSpan.FullLine) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = true).padding(end = 12.dp)
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
                                    .padding(horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) DeepForest else SecondaryText,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(2.dp)
                                        .background(
                                            color = if (isSelected) DeepForest else Color.Transparent,
                                            shape = RoundedCornerShape(1.dp)
                                        )
                                )
                            }
                        }
                    }
                    
                    // Filter ˅
                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showFilterSheet = true }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (filterState.isActive) DeepForest else PrimaryText
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Filter",
                            tint = if (filterState.isActive) DeepForest else PrimaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } // close item

            // 4. PRODUCT FEED OR SKELETON / ERROR / EMPTY STATES
            if (isLoading) {
                items(6) { index ->
                    val height = if (index % 2 == 0) 280 else 320
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(shimmerBrush)
                    )
                }
            } else if (isError) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CloudOff, contentDescription = "Error", modifier = Modifier.size(48.dp), tint = SecondaryText)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Couldn't load catalog", color = PrimaryText, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = DeepForest)) {
                            Text("Retry")
                        }
                    }
                }
            } else if (displayProducts.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No products match your search." else "No products found in this category.",
                            color = SecondaryText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(displayProducts) { product ->
                    DiscoverEditorialCard(
                        product = product,
                        onCardClick = { selectedProduct = product },
                        onTryOn = {
                            if (SessionManager.isLoggedIn) {
                                navController.navigate("try_on/${product.id}")
                            } else {
                                showAccountPrompt = true
                            }
                        },
                        onToggleFavourite = {
                            TiHinStyleRepository.toggleFavourite(product.id)
                        }
                    )
                }
            }
        } // close LazyVerticalStaggeredGrid

        // 5. Peek & Reveal Overlay (STEP 2B)
        AnimatedVisibility(
            visible = selectedProduct != null,
            enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.95f, animationSpec = tween(250, easing = FastOutLinearInEasing)),
            modifier = Modifier.fillMaxSize()
        ) {
            selectedProduct?.let { product ->
                // Dimmed background backdrop
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { selectedProduct = null }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    PeekRevealCard(
                        product = product,
                        onClose = { selectedProduct = null },
                        onTryOn = {
                            if (SessionManager.isLoggedIn) {
                                navController.navigate("try_on/${product.id}")
                            } else {
                                showAccountPrompt = true
                            }
                        },
                        onBuy = { /* No-op for demo */ },
                        onToggleFavourite = {
                            TiHinStyleRepository.toggleFavourite(product.id)
                        }
                    )
                }
            }
        }
        
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = SurfaceColor,
                dragHandle = { BottomSheetDefaults.DragHandle(color = BorderColor) }
            ) {
                DiscoverFilterSheet(
                    currentState = filterState,
                    availableBrands = MockProductDataSource.getProducts().map { it.merchant }.distinct().sorted(),
                    availableSizes = listOf("XS", "S", "M", "L", "XL", "XXL"),
                    availableColors = listOf("Black", "White", "Navy", "Beige", "Red", "Green"),
                    onApply = { newState ->
                        filterState = newState
                        showFilterSheet = false
                    },
                    onClear = {
                        filterState = DiscoverFilterState()
                        showFilterSheet = false
                    }
                )
            }
        }
    }
}

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
                    .height((product.cardHeight * 1.15f).dp)
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
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(48.dp) // 48dp min touch target
                        .clip(CircleShape)
                        .clickable(onClick = onToggleFavourite),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp) // Compact visual
                            .clip(CircleShape)
                            .background(SurfaceColor.copy(alpha = 0.92f))
                            .border(1.dp, BorderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (product.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (product.isFavourite) "Remove from favourites" else "Add to favourites",
                            tint = if (product.isFavourite) DeepForest else PrimaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 10.dp)) {
                Text(
                    text = product.merchant.uppercase(),
                    style = BrandTagStyle,
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(0.dp))
                Text(
                    text = product.name,
                    style = ProductNameStyle,
                    color = PrimaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedPrice,
                    style = PriceStyle,
                    color = PrimaryText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onTryOn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepForest,
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
 * Peek & Reveal"""

new_content = re.sub(pattern, replacement, content, flags=re.DOTALL)
if new_content == content:
    print("Failed")
else:
    with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
        f.write(new_content)
    print("Success")
