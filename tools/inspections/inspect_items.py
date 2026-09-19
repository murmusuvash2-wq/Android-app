with open('discover_copy.txt', 'r') as f:
    content = f.read()

import re
match = re.search(r'(?s)LazyVerticalStaggeredGrid\([\s\S]*?\) \{[\s\S]*?\} // close LazyVerticalStaggeredGrid', content)
if match:
    # Print the first 2000 chars of it to see the structure
    s = match.group(0)
    print(s[:3000])
else:
    print("Not found")
