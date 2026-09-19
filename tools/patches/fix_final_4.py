import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

# Fix credits
pattern_credits = r"val currentCredits by com\.example\.data\.repository\.CreditRepository\.credits\.collectAsStateWithLifecycle\(\)\s*val displayProducts"
replace_credits = "val displayProducts"
content = re.sub(pattern_credits, replace_credits, content)

pattern_pill = r"GlobalCreditPill\([\s\S]*?credits = currentCredits,[\s\S]*?onClick = \{ navController\.navigate\(\"tries_credits\"\) \}[\s\S]*?\)"
replace_pill = """GlobalCreditPill(
                        credits = SessionManager.credits,
                        onClick = { navController.navigate("tries_credits") }
                    )"""
content = re.sub(pattern_pill, replace_pill, content)

# Remove the bad injection in DiscoverEditorialCard
bad_injection = r"contentScale = ContentScale\.Crop,\s*modifier = Modifier\.fillMaxSize\(\)\s*\)\s*if \(scale > 1\.05f\) \{\s*Surface\([\s\S]*?text = \"Reset Zoom\"[\s\S]*?\}\s*\}"
replace_good = """contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )"""
# We only want to replace the FIRST occurrence (which is in DiscoverEditorialCard, wait! The first is in DiscoverEditorialCard? Let's replace only in DiscoverEditorialCard.
# Actually, wait. I want to keep it in PeekRevealCard, and remove it from DiscoverEditorialCard.
# Let's just write a function to do it.

def fix_card(match):
    return """contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )"""

content = content.replace(
"""                            modifier = Modifier.fillMaxSize()
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
                    }""",
"""                            modifier = Modifier.fillMaxSize()
                        )""", 1) # Only first occurrence

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write(content)
print("Done fix")
