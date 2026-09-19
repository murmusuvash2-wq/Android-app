import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

pattern = r"// 3\. TABS \+ FILTER[\s\S]*?\} // close item"

replacement = """// 3. TABS + FILTER
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = true).padding(end = 12.dp)
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
                                            .padding(horizontal = 2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = tab.title,
                                            fontSize = 11.sp,
                                            fontFamily = Inter,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) DeepForest else SecondaryText,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(20.dp)
                                                .height(2.dp)
                                                .background(
                                                    color = if (isSelected) DeepForest else Color.Transparent,
                                                    shape = RoundedCornerShape(1.dp)
                                                )
                                        )
                                    }
                                }
                            }
                            
                            // Filter ˅
                            Row(
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { showFilterSheet = true }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Filter",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (filterState.isActive) DeepForest else PrimaryText
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Filter",
                                    tint = if (filterState.isActive) DeepForest else PrimaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } // close item"""

new_content = re.sub(pattern, replacement, content, flags=re.DOTALL)
if new_content == content:
    print("Failed")
else:
    with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
        f.write(new_content)
    print("Success")

