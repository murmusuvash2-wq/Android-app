import re

with open('discover_copy.txt', 'r') as f:
    content = f.read()

for match in re.finditer(r'LazyVerticalStaggeredGrid', content):
    start = max(0, match.start() - 300)
    end = min(len(content), match.end() + 700)
    print(f"--- Found {match.group(0)} ---")
    print(content[start:end])
    print("="*40)
