import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

# Let's replace the missing filter options
new_filters = """        // Brands
        if (availableBrands.isNotEmpty()) {
            Text("Brand", style = Typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = PrimaryText)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableBrands.forEach { brand ->
                    val isSelected = brand in tempState.selectedBrands
                    Surface(
                        onClick = {
                            val newBrands = if (isSelected) tempState.selectedBrands - brand else tempState.selectedBrands + brand
                            tempState = tempState.copy(selectedBrands = newBrands)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) DeepForest else SurfaceVariantColor,
                        border = BorderStroke(1.dp, if (isSelected) DeepForest else BorderColor)
                    ) {
                        Text(
                            text = brand,
                            color = if (isSelected) Color.White else PrimaryText,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sizes
        if (availableSizes.isNotEmpty()) {
            Text("Size", style = Typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = PrimaryText)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableSizes.forEach { size ->
                    val isSelected = size in tempState.selectedSizes
                    Surface(
                        onClick = {
                            val newSizes = if (isSelected) tempState.selectedSizes - size else tempState.selectedSizes + size
                            tempState = tempState.copy(selectedSizes = newSizes)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) DeepForest else SurfaceVariantColor,
                        border = BorderStroke(1.dp, if (isSelected) DeepForest else BorderColor)
                    ) {
                        Text(
                            text = size,
                            color = if (isSelected) Color.White else PrimaryText,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Colors
        if (availableColors.isNotEmpty()) {
            Text("Color", style = Typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = PrimaryText)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableColors.forEach { colorStr ->
                    val isSelected = colorStr in tempState.selectedColors
                    Surface(
                        onClick = {
                            val newColors = if (isSelected) tempState.selectedColors - colorStr else tempState.selectedColors + colorStr
                            tempState = tempState.copy(selectedColors = newColors)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) DeepForest else SurfaceVariantColor,
                        border = BorderStroke(1.dp, if (isSelected) DeepForest else BorderColor)
                    ) {
                        Text(
                            text = colorStr,
                            color = if (isSelected) Color.White else PrimaryText,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }"""

# find the old Brand block to replace it
brand_pattern = r'// Brands.*?Spacer\(modifier = Modifier\.height\(16\.dp\)\)\n\s*\}'
content = re.sub(brand_pattern, new_filters, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write(content)

