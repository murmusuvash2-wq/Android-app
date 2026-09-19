import re

with open('app/src/test/java/com/example/DiscoverScreenTest.kt', 'r') as f:
    content = f.read()

content = content.replace('composeTestRule.onNodeWithText("ZARA").performClick()', 'composeTestRule.onAllNodesWithText("ZARA")[1].performClick()')

with open('app/src/test/java/com/example/DiscoverScreenTest.kt', 'w') as f:
    f.write(content)

