import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

pattern = r"(\.graphicsLayer \{\s*scaleX = scale\s*scaleY = scale\s*translationX = offsetX\s*translationY = offsetY\s*\},.*?contentAlignment = Alignment\.Center\s*\)\s*\{[\s\S]*?AsyncImage\([\s\S]*?\)[\s\S]*?\})"

replacement = r"""\1
                    if (scale > 1.05f) {
                        Surface(
                            onClick = { 
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                                isUserInteracting = false
                            },
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "Reset Zoom",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }"""

new_content = re.sub(pattern, replacement, content, flags=re.DOTALL)
if new_content == content:
    print("Failed")
else:
    with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
        f.write(new_content)
    print("Success")

