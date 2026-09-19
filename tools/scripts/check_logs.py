import os

logs = []
for root, dirs, files in os.walk('.'):
    for f in files:
        if f.endswith('.log') or 'crash' in f.lower():
            logs.append(os.path.join(root, f))

found = False
for log in logs:
    try:
        with open(log, 'r') as f:
            content = f.read()
            if 'FATAL EXCEPTION' in content or 'AndroidRuntime' in content or 'DiscoverScreen' in content:
                # Need to be careful not to print too much
                lines = content.split('\n')
                for i, line in enumerate(lines):
                    if 'FATAL EXCEPTION' in line or 'AndroidRuntime' in line or 'Exception' in line:
                        if 'DiscoverScreen' in '\n'.join(lines[i:i+50]):
                            print(f"--- Found in {log} ---")
                            print('\n'.join(lines[i:i+30]))
                            found = True
                            break
    except Exception as e:
        pass

if not found:
    print("NO RUNTIME STACK TRACE AVAILABLE")
