import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

pattern = r"// Sizes"
replacement = """// Price Range
        Text("Price", style = Typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = PrimaryText)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val priceRanges = listOf("Under ₹3K" to 3000.0, "Under ₹5K" to 5000.0, "Under ₹10K" to 10000.0)
            priceRanges.forEach { (label, value) ->
                val isSelected = tempState.maxPrice == value
                Surface(
                    onClick = {
                        tempState = tempState.copy(maxPrice = if (isSelected) null else value)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) DeepForest else SurfaceVariantColor,
                    border = BorderStroke(1.dp, if (isSelected) DeepForest else BorderColor)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else PrimaryText,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sizes"""

new_content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write(new_content)
print("Done filter")
