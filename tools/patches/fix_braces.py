import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

# I will just replace the specific block.
pattern = r"""                        }
   
                        Surface\(onClick = \{ showFilterSheet = true \}, shape = RoundedCornerShape\(12\.dp\), color = if \(filterState\.isActive\) DeepForest else SurfaceColor, border = BorderStroke\(1\.dp, if \(filterState\.isActive\) DeepForest else BorderColor\), modifier = Modifier\.padding\(start = 12\.dp\)\.height\(40\.dp\)\) \{
                            Row\(modifier = Modifier\.padding\(horizontal = 12\.dp, vertical = 8\.dp\), verticalAlignment = Alignment\.CenterVertically\) \{
                                Icon\(imageVector = androidx\.compose\.material\.icons\.filled\.FilterList, contentDescription = "Filter", tint = if \(filterState\.isActive\) Color\.White else PrimaryText, modifier = Modifier\.size\(16\.dp\)\)
                                if \(filterState\.isActive\) \{
                                    Spacer\(modifier = Modifier\.width\(6\.dp\)\)
                                    Text\(text = "On", fontSize = 12\.sp, fontWeight = FontWeight\.SemiBold, color = Color\.White\)
                                \}
                            \}
                        \}
                    \}
                    // 4. PRODUCT FEED"""

# It's better to just do string replacement
# wait, the spacing might be different. Let's use regex that ignores exact spacing.
# Actually I'll just use a precise replace.
