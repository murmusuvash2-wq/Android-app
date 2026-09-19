import re

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'r') as f:
    content = f.read()

# Fix Try On and Buy colors in PeekRevealCard
pattern_btns = r"// Primary Action: Try On[\s\S]*?// Secondary Action: Buy ↗[\s\S]*?OutlinedButton\([\s\S]*?onClick = onBuy,[\s\S]*?shape = RoundedCornerShape\(12\.dp\),[\s\S]*?border = BorderStroke\(1\.dp, BorderColor\),[\s\S]*?colors = ButtonDefaults\.outlinedButtonColors\([\s\S]*?containerColor = SurfaceVariantColor,[\s\S]*?contentColor = PrimaryText[\s\S]*?\),[\s\S]*?modifier = Modifier[\s\S]*?\.weight\(0\.4f\)[\s\S]*?\.height\(48\.dp\)[\s\S]*?\) \{[\s\S]*?Text\([\s\S]*?text = \"Buy ↗\",[\s\S]*?fontSize = 13\.sp,[\s\S]*?fontWeight = FontWeight\.SemiBold,[\s\S]*?color = PrimaryText,[\s\S]*?maxLines = 1[\s\S]*?\)[\s\S]*?\}"
replace_btns = """// Primary Action: Try On
                    Button(
                        onClick = onTryOn,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepForest,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(0.6f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Try On",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }

                    // Secondary Action: Buy ↗
                    Button(
                        onClick = onBuy,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurchaseCTA,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(0.4f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Buy ↗",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }"""

content = re.sub(pattern_btns, replace_btns, content, flags=re.DOTALL)

# Remove Style tip
pattern_style_tip = r"if \(product\.styleTip != null\) \{[\s\S]*?Text\([\s\S]*?text = \"Style Tip\",[\s\S]*?style = Typography\.labelSmall,[\s\S]*?color = PrimaryText[\s\S]*?\)[\s\S]*?Spacer\(modifier = Modifier\.height\(4\.dp\)\)[\s\S]*?Text\([\s\S]*?text = product\.styleTip,[\s\S]*?style = BodyContentStyle,[\s\S]*?color = SecondaryText[\s\S]*?\)[\s\S]*?\}"
content = re.sub(pattern_style_tip, "", content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/DiscoverScreen.kt', 'w') as f:
    f.write(content)
print("Done peek")
