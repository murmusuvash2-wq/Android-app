def calculate_tabs(screen_width):
    # approximate text widths at 12sp + 8dp padding (4dp on each side)
    # Assume 1 char = ~6dp on average at 12sp (Inter is a bit wide, let's say 6.5dp)
    char_w = 6.5
    tabs = [
        int(len("Trending Now") * char_w) + 8,
        int(len("Most Loved") * char_w) + 8,
        int(len("Best Sellers") * char_w) + 8,
        int(len("Just In") * char_w) + 8
    ]
    
    total_tabs = sum(tabs)
    
    print(f"\n--- Screen Width: {screen_width}dp ---")
    
    # 16dp horizontal padding on the LazyVerticalStaggeredGrid: 
    # BUT wait, the row is INSIDE an item of the grid. 
    # The grid has `contentPadding = PaddingValues(start = 16.dp, end = 16.dp)`.
    # So the Row width is screen_width - 32.
    available_width = screen_width - 32
    
    print(f"Available Row Width: {available_width}dp")
    print(f"Total Tabs Width (approx): {total_tabs}dp")
    print(f"Individual Tab Widths: {tabs}")
    
    if total_tabs > available_width:
        print("WARNING: Tabs exceed available width! Will clip/wrap/overflow.")
        
    gap = (available_width - total_tabs) / 5.0 # SpaceEvenly divides by N+1 gaps
    print(f"SpaceEvenly Gap: {gap:.1f}dp")
    
    pos = gap
    for i, t in enumerate(tabs):
        print(f"  Tab {i+1}: left={pos:.1f}, right={pos+t:.1f}, width={t}")
        pos += t + gap
        
calculate_tabs(360)
calculate_tabs(375)
calculate_tabs(390)
calculate_tabs(412)
calculate_tabs(430)
