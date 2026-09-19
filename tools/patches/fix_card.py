import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

pattern_heart = r"// Favorite Action Button - isolated touch target[\s\S]*?\}[\s\S]*?\}[\s\S]*?\}"
replace_heart = """// Favorite Action Button - isolated touch target
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
                            imageVector = if (product.isFavourite) androidx.compose.material.icons.Icons.Default.Favorite else androidx.compose.material.icons.Icons.Default.FavoriteBorder,
                            contentDescription = if (product.isFavourite) "Remove from favourites" else "Add to favourites",
                            tint = if (product.isFavourite) DeepForest else PrimaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }"""
content = re.sub(pattern_heart, replace_heart, content, flags=re.DOTALL, count=1)

# Now fix Try On CTA container color
pattern_try_on = r"Button\([\s\S]*?onClick = onTryOn,[\s\S]*?colors = ButtonDefaults\.buttonColors\([\s\S]*?containerColor = PurchaseCTA,[\s\S]*?contentColor = Color\.White[\s\S]*?\),[\s\S]*?shape = RoundedCornerShape\(10\.dp\),[\s\S]*?contentPadding = PaddingValues\(horizontal = 12\.dp, vertical = 8\.dp\),[\s\S]*?modifier = Modifier[\s\S]*?\.fillMaxWidth\(\)[\s\S]*?\.height\(48\.dp\)[\s\S]*?\) \{"
replace_try_on = """Button(
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
                ) {"""
content = re.sub(pattern_try_on, replace_try_on, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write(content)
print("Done card")
