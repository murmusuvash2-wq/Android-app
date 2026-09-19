import re

def replace_pill(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Search for the Surface that acts as the credit pill
    pattern = r'Surface\(\s*onClick = \{ navController\.navigate\(Screen\.TriesCredits\.route\) \},\s*shape = RoundedCornerShape\(20\.dp\),\s*color = [^,]+,\s*border = BorderStroke\([^)]+\)[\s\S]*?Icon\([\s\S]*?text = "\$\{SessionManager\.credits\} Credits"[\s\S]*?\}\s*\}'
    
    match = re.search(pattern, content)
    if match:
        new_pill = """GlobalCreditPill(
                    credits = SessionManager.credits,
                    onClick = { navController.navigate(Screen.TriesCredits.route) }
                )"""
        # Indent it according to the matched string
        spaces = match.group(0)[:len(match.group(0)) - len(match.group(0).lstrip())]
        new_pill = "\n".join([spaces + line if i > 0 else line for i, line in enumerate(new_pill.split("\n"))])
        
        content = content.replace(match.group(0), new_pill)
        
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"Updated {filepath}")
    else:
        print(f"Could not find pill in {filepath}")

replace_pill('app/src/main/java/com/example/ui/HomeScreen.kt')
replace_pill('app/src/main/java/com/example/ui/LooksScreen.kt')
replace_pill('app/src/main/java/com/example/ui/MeScreen.kt')

