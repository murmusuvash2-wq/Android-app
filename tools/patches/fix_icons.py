import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.Default.List", "androidx.compose.material.icons.Icons.Default.List")
# I'll just check if it compiles

