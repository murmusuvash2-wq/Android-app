import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

# Fix the broken import
content = content.replace("import androidx.compose.material.icons.filled.List\n.Default.List", "import androidx.compose.material.icons.filled.List")
content = content.replace("import Icons.Default.List", "")

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write(content)

