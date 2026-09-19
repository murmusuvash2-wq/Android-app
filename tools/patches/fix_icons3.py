import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.material.icons.Icons" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.FavoriteBorder", "import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.FavoriteBorder")

# also fix the repeatable annotation!
lines = content.split('\n')
final_lines = []
for i, line in enumerate(lines):
    if line.strip() == "@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)":
        if i+1 < len(lines) and ("fun DiscoverScreen" in lines[i+1] or "fun DiscoverFilterSheet" in lines[i+1]):
            # only keep if it is right before the fun
            if not final_lines or final_lines[-1].strip() != "@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)":
                final_lines.append(line)
    else:
        final_lines.append(line)

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write('\n'.join(final_lines))

