                val currentShowcase = showcaseDataList[page]
                val product = remember(currentShowcase.productId) { ProductRepository.get().getProductById(currentShowcase.productId) }

                if (product != null) {
                    Column {
                        val hangerImg = currentShowcase.hangerImage ?: product.productImages.getOrNull(1)
                        val wornImg = currentShowcase.wornImage ?: product.productImages.getOrNull(0) ?: product.primaryImageUrl
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        ) {
                            if (hangerImg != null && wornImg != null && hangerImg != wornImg) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Left Image: Product outfit
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(hangerImg)
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
                                            .data(wornImg)
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
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Try-on arrow",
                                        tint = DeepForest,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                // Single-image fallback
                                val primaryImg = wornImg ?: hangerImg ?: product.primaryImageUrl
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(primaryImg)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Showcase primary view",
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.TopCenter,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(OffWhite)
                                        .border(1.dp, WarmGray, RoundedCornerShape(16.dp))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Info and CTA row (compact, showing title, price, and CTA)
                        val heroPriceFormatted = remember(product.price) {
                            NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
                                maximumFractionDigits = 0
                            }.format(product.price)
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
                                val primaryImg = hangerImg ?: wornImg ?: product.primaryImageUrl
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(primaryImg)
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
                                        text = product.name,
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
                                    TryOnManager.selectedProductId = product.id
                                    TryOnManager.selectedProductName = product.name
                                    TryOnManager.selectedProductBrand = product.brand
                                    TryOnManager.selectedProductPrice = product.price
                                    TryOnManager.selectedProductImage = product.primaryImageUrl
                                    TryOnManager.generatedResultImageUri = wornImg ?: product.primaryImageUrl
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
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
