with open('discover_copy.txt', 'r') as f:
    content = f.read()

import re
match = re.search(r'(?s)items\(displayProducts, key = \{ it\.id \} \) \{ product ->[\s\S]*?\} // close LazyVerticalStaggeredGrid', content)
if match:
    s = match.group(0)
    print(s[:3000])
else:
    print("Not found")
