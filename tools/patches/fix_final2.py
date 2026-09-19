import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

# Add credits state to DiscoverScreen
pattern_state = r"val displayProducts = remember\(products, selectedTab, searchQuery, filterState\) \{"
replace_state = """val currentCredits by com.example.data.repository.CreditRepository.credits.collectAsStateWithLifecycle()
    val displayProducts = remember(products, selectedTab, searchQuery, filterState) {"""
content = content.replace("val displayProducts = remember(products, selectedTab, searchQuery, filterState) {", replace_state)

# Update GlobalCreditPill call
pattern_pill = r"GlobalCreditPill\(\)"
replace_pill = """GlobalCreditPill(
                        credits = currentCredits,
                        onClick = { navController.navigate("tries_credits") }
                    )"""
content = content.replace("GlobalCreditPill()", replace_pill)

# Import collectAsStateWithLifecycle
if "import androidx.lifecycle.compose.collectAsStateWithLifecycle" not in content:
    content = content.replace("import com.example.ui.SessionManager", "import com.example.ui.SessionManager\nimport androidx.lifecycle.compose.collectAsStateWithLifecycle")

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write(content)
print("Done final 2")
