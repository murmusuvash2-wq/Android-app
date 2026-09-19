# Simulate tab widths
tabs = [12, 10, 12, 7]
total_width = 100
total_tabs_width = sum(tabs)

# SpaceBetween
gap_sb = (total_width - total_tabs_width) / (len(tabs) - 1)
print("SpaceBetween:")
pos = 0
for t in tabs:
    print(f"Tab size {t}, pos: {pos:.1f} to {pos+t:.1f}, center: {pos + t/2:.1f}")
    pos += t + gap_sb

# SpaceEvenly
gap_se = (total_width - total_tabs_width) / (len(tabs) + 1)
print("\nSpaceEvenly:")
pos = gap_se
for t in tabs:
    print(f"Tab size {t}, pos: {pos:.1f} to {pos+t:.1f}, center: {pos + t/2:.1f}")
    pos += t + gap_se

# Equal Weights (current)
box_w = total_width / len(tabs)
print("\nEqual Weights:")
for i, t in enumerate(tabs):
    box_start = i * box_w
    box_end = box_start + box_w
    center = box_start + box_w / 2
    print(f"Tab size {t}, center: {center:.1f}, pos: {center - t/2:.1f} to {center + t/2:.1f}")
