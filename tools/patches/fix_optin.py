import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.material3.ExperimentalMaterial3Api::class", "@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)")

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write(content)

