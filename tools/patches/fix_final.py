import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

imports = """
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.KeyboardArrowDown
import com.example.ui.SessionManager
"""
content = content.replace("import com.example.ui.theme.*", "import com.example.ui.theme.*\n" + imports)

shimmer_code = """
@Composable
fun rememberShimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )
    val transition = rememberInfiniteTransition(label = "")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
}
"""

if "fun rememberShimmerBrush" not in content:
    content += "\n" + shimmer_code

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write(content)
print("Done final")
