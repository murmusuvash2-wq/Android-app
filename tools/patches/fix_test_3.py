import re

with open('app/src/test/java/com/example/DiscoverScreenTest.kt', 'r') as f:
    content = f.read()

content = content.replace('composeTestRule.onAllNodesWithText("ZARA")[1].performClick()', 'composeTestRule.onAllNodesWithText("ZARA").onLast().performClick()')
content = content.replace('import androidx.compose.ui.test.onAllNodesWithText', 'import androidx.compose.ui.test.onAllNodesWithText\nimport androidx.compose.ui.test.onLast')

with open('app/src/test/java/com/example/DiscoverScreenTest.kt', 'w') as f:
    f.write(content)

