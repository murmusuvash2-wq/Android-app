import re

with open('app/src/main/java/com/example/ui/HomeScreen.kt', 'r') as f:
    content = f.read()

target = """        // 3. ACTION BUTTONS: Take Photo & Gallery
        val defaultPhotoUri = UserPhotosRepository.defaultPhotoUri

        Row("""

replacement = """        // 3. ACTION BUTTONS: Take Photo & Gallery
        Row("""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/HomeScreen.kt', 'w') as f:
        f.write(content)
    print("Cleaned up unused variable")
else:
    print("Target not found")
