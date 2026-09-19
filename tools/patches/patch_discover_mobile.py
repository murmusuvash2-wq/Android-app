import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

changes_made = 0

# 1. Search Bottom Padding
search_target = """                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                selectedProduct = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),"""

search_replace = """                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                selectedProduct = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),"""
if search_target in content:
    content = content.replace(search_target, search_replace)
    changes_made += 1
else:
    print("Failed to match search target")

# 2. Tabs Row Layout and Padding
tabs_target = """                    // 3. TABS (Fixed single-row compact editorial tabs, all 4 visible without scrolling)
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DiscoverTab.values().forEach { tab ->
                                val isSelected = selectedTab == tab
                                Column(
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            selectedTab = tab
                                            selectedProduct = null
                                        }
                                        .padding(horizontal = 4.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {"""

tabs_replace = """                    // 3. TABS (Fixed single-row compact editorial tabs, all 4 visible without scrolling)
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DiscoverTab.values().forEach { tab ->
                                val isSelected = selectedTab == tab
                                Column(
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            selectedTab = tab
                                            selectedProduct = null
                                        }
                                        .padding(horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {"""
if tabs_target in content:
    content = content.replace(tabs_target, tabs_replace)
    changes_made += 1
else:
    print("Failed to match tabs target")

# 3. Tab Indicator Width
indicator_target = """                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.6f)
                                            .height(2.dp)"""

indicator_replace = """                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height(2.dp)"""
if indicator_target in content:
    content = content.replace(indicator_target, indicator_replace)
    changes_made += 1
else:
    print("Failed to match indicator target")

# 4. Favorite Action Button Interactive Size
fav_target = """                // Favorite Action Button - isolated touch target
                Surface(
                    onClick = onToggleFavourite,
                    shape = CircleShape,
                    color = SurfaceColor.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(36.dp)
                        .align(Alignment.TopEnd)
                ) {"""

fav_replace = """                // Favorite Action Button - isolated touch target
                Surface(
                    onClick = onToggleFavourite,
                    shape = CircleShape,
                    color = SurfaceColor.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier
                        .padding(8.dp)
                        .minimumInteractiveComponentSize()
                        .size(36.dp)
                        .align(Alignment.TopEnd)
                ) {"""
if fav_target in content:
    content = content.replace(fav_target, fav_replace)
    changes_made += 1
else:
    print("Failed to match fav target")

# 5. Bottom Card Padding
padding_target = """            Column(modifier = Modifier.padding(12.dp)) {
                Text("""
padding_replace = """            Column(modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 16.dp)) {
                Text("""
if padding_target in content:
    content = content.replace(padding_target, padding_replace)
    changes_made += 1
else:
    print("Failed to match bottom padding target")

# 6. Try On CTA Height
cta_target = """                Button(
                    onClick = onTryOn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurchaseCTA,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {"""
cta_replace = """                Button(
                    onClick = onTryOn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurchaseCTA,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {"""
if cta_target in content:
    content = content.replace(cta_target, cta_replace)
    changes_made += 1
else:
    print("Failed to match CTA height target")

if changes_made > 0:
    with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
        f.write(content)
    print(f"Applied {changes_made} changes.")

