with open('discover_copy.txt', 'r') as f:
    content = f.read()

import re
match = re.search(r'(?s)fun DiscoverScreen.*?Box\(modifier = Modifier\.fillMaxSize\(\)\.background\(BackgroundColor\)\)', content)
if match:
    print(match.group(0))
else:
    print("Not found")
