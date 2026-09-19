import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

# 1. Add DiscoverFilterState data class at top level if not present
if "data class DiscoverFilterState" not in content:
    filter_state_code = """
data class DiscoverFilterState(
    val selectedBrands: Set<String> = emptySet(),
    val maxPrice: Double? = null,
    val selectedSizes: Set<String> = emptySet(),
    val selectedColors: Set<String> = emptySet()
) {
    val isActive: Boolean
        get() = selectedBrands.isNotEmpty() || maxPrice != null || selectedSizes.isNotEmpty() || selectedColors.isNotEmpty()
}
"""
    content = content.replace('enum class DiscoverTab(val title: String) {\n', filter_state_code + '\n' + 'enum class DiscoverTab(val title: String) {\n')

# 2. Update displayProducts to include filtering
old_displayProducts = """
    val displayProducts = remember(products, selectedTab, searchQuery) {
        val tabFiltered = getProductsForTab(products, selectedTab)
        if (searchQuery.isBlank()) {
            tabFiltered
        } else {
            tabFiltered.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.merchant.contains(searchQuery, ignoreCase = true)
            }
        }
    }
"""
new_displayProducts = """
    var filterState by remember { mutableStateOf(DiscoverFilterState()) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val displayProducts = remember(products, selectedTab, searchQuery, filterState) {
        val tabFiltered = getProductsForTab(products, selectedTab)
        var result = tabFiltered
        
        if (searchQuery.isNotBlank()) {
            result = result.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.merchant.contains(searchQuery, ignoreCase = true)
            }
        }
        
        if (filterState.selectedBrands.isNotEmpty()) {
            result = result.filter { it.merchant in filterState.selectedBrands }
        }
        if (filterState.maxPrice != null) {
            result = result.filter { it.price <= filterState.maxPrice!! }
        }
        if (filterState.selectedSizes.isNotEmpty()) {
            result = result.filter { product -> 
                product.sizes != null && product.sizes.any { it in filterState.selectedSizes }
            }
        }
        if (filterState.selectedColors.isNotEmpty()) {
            result = result.filter { product -> 
                product.colors != null && product.colors.any { it in filterState.selectedColors }
            }
        }
        
        result
    }
"""
content = content.replace(old_displayProducts.strip(), new_displayProducts.strip())

# 3. Replace GlobalCreditPill in DiscoverScreen
old_credit_pill = """
                            if (!SessionManager.isGuest) {
                                Surface(
                                    onClick = { navController.navigate(Screen.TriesCredits.route) },
                                    shape = RoundedCornerShape(20.dp),
                                    color = SurfaceColor,
                                    border = BorderStroke(1.dp, BorderColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Credits",
                                            tint = ChampagneGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${SessionManager.credits} Credits",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PrimaryText
                                        )
                                    }
                                }
                            }
"""
new_credit_pill = """
                            if (!SessionManager.isGuest) {
                                GlobalCreditPill(
                                    credits = SessionManager.credits,
                                    onClick = { navController.navigate(Screen.TriesCredits.route) }
                                )
                            }
"""
content = content.replace(old_credit_pill.strip(), new_credit_pill.strip())

# 4. Modify Tabs row to include Filter button
old_tabs_row = """
                    // 3. TABS (Fixed single-row compact editorial tabs, all 4 visible without scrolling)
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DiscoverTab.values().forEach { tab ->
"""
new_tabs_row = """
                    // 3. TABS + FILTER
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                DiscoverTab.values().forEach { tab ->
"""
content = content.replace(old_tabs_row.strip(), new_tabs_row.strip())

old_tabs_end = """
                                }
                            }
                        }
                    }
"""
new_tabs_end = """
                                }
                            }
                        }
                        
                        // Filter Button
                        Surface(
                            onClick = { showFilterSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            color = if (filterState.isActive) DeepForest else SurfaceColor,
                            border = BorderStroke(1.dp, if (filterState.isActive) DeepForest else BorderColor),
                            modifier = Modifier.padding(start = 12.dp).height(40.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = if (filterState.isActive) Color.White else PrimaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                                if (filterState.isActive) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "On",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
"""
# Handled via regex for precision
content = re.sub(r'(\s*)\}(\s*)\}(\s*)\}(\s*)\}(\s*)// 4\. PRODUCT FEED', 
                 r'\1}\2}\3}\n                        \n                        Surface(onClick = { showFilterSheet = true }, shape = RoundedCornerShape(12.dp), color = if (filterState.isActive) DeepForest else SurfaceColor, border = BorderStroke(1.dp, if (filterState.isActive) DeepForest else BorderColor), modifier = Modifier.padding(start = 12.dp).height(40.dp)) {\n                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {\n                                Icon(imageVector = androidx.compose.material.icons.Icons.Default.FilterList, contentDescription = "Filter", tint = if (filterState.isActive) Color.White else PrimaryText, modifier = Modifier.size(16.dp))\n                                if (filterState.isActive) {\n                                    Spacer(modifier = Modifier.width(6.dp))\n                                    Text(text = "On", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)\n                                }\n                            }\n                        }\n\4}\5// 4. PRODUCT FEED', 
                 content)


# 5. Add BottomSheet
sheet_code = """
    if (showFilterSheet) {
        androidx.compose.material3.ExperimentalMaterial3Api::class
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = SurfaceColor,
            dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
        ) {
            DiscoverFilterSheet(
                currentState = filterState,
                availableBrands = products.map { it.merchant }.toSet().sorted(),
                availableSizes = products.flatMap { it.sizes ?: emptyList() }.toSet().sorted(),
                availableColors = products.flatMap { it.colors ?: emptyList() }.toSet().sorted(),
                onApply = { newState ->
                    filterState = newState
                    showFilterSheet = false
                },
                onClear = {
                    filterState = DiscoverFilterState()
                }
            )
        }
    }
"""
content = content.replace("Box(", sheet_code + "\n    Box(", 1) # Insert before Box(modifier = Modifier.fillMaxSize()

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write(content)

