with open('app/src/main/java/com/example/ui/HomeScreen.kt', 'r') as f:
    lines = f.readlines()

# insert a closing brace after line 327
lines.insert(327, '                        }\n')

with open('app/src/main/java/com/example/ui/HomeScreen.kt', 'w') as f:
    f.writelines(lines)
