import re

with open('app/src/main/java/com/example/ui/HomeScreen.kt', 'r') as f:
    content = f.read()

target1 = "    val showcaseDataList = remember { ProductRepository.get().getHeroLooks() }"
replacement1 = """    val showcaseDataList = remember { 
        ProductRepository.get().getHeroLooks().filter { look ->
            val product = ProductRepository.get().getProductById(look.productId)
            val hangerImg = look.hangerImage ?: product?.productImages?.getOrNull(1)
            val wornImg = look.wornImage ?: product?.productImages?.getOrNull(0) ?: product?.primaryImageUrl
            hangerImg != null && wornImg != null && hangerImg != wornImg
        }
    }"""

target2 = """                        Box(
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
                                        imageVector = Icons.Default.ArrowForward,
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
                        }"""
replacement2 = """                        Box(
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
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Try-on arrow",
                                        tint = DeepForest,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }"""

if target1 in content:
    content = content.replace(target1, replacement1)
    print("Replaced target1")
if target2 in content:
    content = content.replace(target2, replacement2)
    print("Replaced target2")
with open('app/src/main/java/com/example/ui/HomeScreen.kt', 'w') as f:
    f.write(content)
