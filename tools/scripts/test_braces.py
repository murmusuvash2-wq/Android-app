import sys
text = sys.stdin.read()
lines = text.split('\n')
open_c = 0
close_c = 0
for i, line in enumerate(lines, 1):
    open_c += line.count('{')
    close_c += line.count('}')
    if "SnackbarHost(" in line:
        print(f'Line {i}: Net {open_c - close_c}')
