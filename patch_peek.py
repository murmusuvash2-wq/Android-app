import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

start_str = "fun PeekRevealCard("
idx_start = content.find(start_str)
idx = idx_start + len(start_str)
while content[idx] != '{':
    idx += 1
brace_count = 1
idx += 1
while brace_count > 0 and idx < len(content):
    if content[idx] == '{':
        brace_count += 1
    elif content[idx] == '}':
        brace_count -= 1
    idx += 1
idx_end = idx

original_func = content[idx_start:idx_end]

new_func = """fun PeekRevealCard(
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
}"""

content = content.replace(original_func, new_func)

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write(content)

print("PeekRevealCard replaced.")
