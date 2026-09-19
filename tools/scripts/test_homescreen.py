with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

target = """                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DiscoverTab.values().forEach { tab ->
                                val isSelected = selectedTab == tab
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable("""

replacement = """                        Row(
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
                                        .clickable("""

if target in content:
    print("Found target")
else:
    print("Target not found")
