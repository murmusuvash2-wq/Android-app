def calculate(total_width, tabs, mode="weight"):
    if mode == "weight":
        print(f"\n--- Mode: {mode} ---")
        col_w = total_width / len(tabs)
        for i, t in enumerate(tabs):
            center = (i + 0.5) * col_w
            left = center - t / 2
            right = center + t / 2
            print(f"Tab {i+1} ({t}w): left={left:.1f}, center={center:.1f}, right={right:.1f}")
    elif mode == "space-evenly":
        print(f"\n--- Mode: {mode} ---")
        total_t = sum(tabs)
        gap = (total_width - total_t) / (len(tabs) + 1)
        pos = gap
        for i, t in enumerate(tabs):
            left = pos
            right = pos + t
            center = pos + t / 2
            print(f"Tab {i+1} ({t}w): left={left:.1f}, center={center:.1f}, right={right:.1f}")
            pos += t + gap
    elif mode == "space-around":
        print(f"\n--- Mode: {mode} ---")
        total_t = sum(tabs)
        gap = (total_width - total_t) / len(tabs)
        pos = gap / 2
        for i, t in enumerate(tabs):
            left = pos
            right = pos + t
            center = pos + t / 2
            print(f"Tab {i+1} ({t}w): left={left:.1f}, center={center:.1f}, right={right:.1f}")
            pos += t + gap
    elif mode == "space-between":
        print(f"\n--- Mode: {mode} ---")
        total_t = sum(tabs)
        gap = (total_width - total_t) / (len(tabs) - 1)
        pos = 0
        for i, t in enumerate(tabs):
            left = pos
            right = pos + t
            center = pos + t / 2
            print(f"Tab {i+1} ({t}w): left={left:.1f}, center={center:.1f}, right={right:.1f}")
            pos += t + gap

calculate(328, [90, 80, 90, 50], "weight")
calculate(328, [90, 80, 90, 50], "space-evenly")
calculate(328, [90, 80, 90, 50], "space-around")
calculate(328, [90, 80, 90, 50], "space-between")

