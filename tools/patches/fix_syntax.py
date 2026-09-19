import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

pattern = r"contentDescription = \"\$\{product\.name\}\n\s*if \(scale > 1\.05f\) \{[\s\S]*?\} - view \$\{page \+ 1\} of \$\{images\.size\}\","
replacement = """contentDescription = "${product.name} - view ${page + 1} of ${images.size}","""
content = re.sub(pattern, replacement, content)

# Now inject the reset zoom AFTER the AsyncImage
pattern2 = r"contentScale = ContentScale\.Crop,\n\s*modifier = Modifier\.fillMaxSize\(\)\n\s*\)"
replacement2 = """contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
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
content = re.sub(pattern2, replacement2, content)

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write(content)
print("Done syntax")
