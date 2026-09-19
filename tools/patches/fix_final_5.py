import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("SessionManager.isLoggedIn", "!SessionManager.isGuest")

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write(content)
print("Done fix")
