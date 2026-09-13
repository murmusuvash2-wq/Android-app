import re

with open('app/src/test/java/com/example/PeekRevealTest.kt', 'r') as f:
    content = f.read()

content = content.replace('imageUrl = "https://example.com/image.jpg",', '')
content = content.replace('imageUrl = "https://example.com/image1.jpg",', '')

with open('app/src/test/java/com/example/PeekRevealTest.kt', 'w') as f:
    f.write(content)
