package com.example.ui

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.*

/**
 * Clean data model representing credit packs available for purchase.
 * Allows easy modification of prices and package tiers without touching UI logic.
 */
data class CreditPack(
    val id: String,
    val credits: Int,
    val priceDisplay: String,
    val featureTag: String = "No ads",
    val isPopular: Boolean = false,
    val buttonText: String
)

val DEFAULT_CREDIT_PACKS = listOf(
    CreditPack(
        id = "pack_5",
        credits = 5,
        priceDisplay = "₹49",
        featureTag = "No ads",
        isPopular = false,
        buttonText = "Buy 5 Credits"
    ),
    CreditPack(
        id = "pack_15",
        credits = 15,
        priceDisplay = "₹99",
        featureTag = "No ads",
        isPopular = true,
        buttonText = "Buy 15 Credits"
    )
)

/**
 * Data model for TiHin Premium tier benefits.
 */
data class PremiumPlan(
    val title: String = "TiHin Premium",
    val priceDisplay: String = "₹299",
    val periodDisplay: String = "/ month",
    val subtitle: String = "Get more from TiHin",
    val benefits: List<String> = listOf(
        "Monthly credits",
        "Ad-free experience",
        "No watermark downloads",
        "Higher-quality downloads",
        "Priority AI processing",
        "New premium features included"
    ),
    val ctaText: String = "Get Premium"
)

/**
 * Data model representing an entry in the user's credit activity history.
 */
data class CreditHistoryItem(
    val id: String,
    val amountDisplay: String,
    val isPositive: Boolean,
    val title: String,
    val timestamp: String
)

val MOCK_CREDIT_HISTORY = listOf(
    CreditHistoryItem(
        id = "tx_1",
        amountDisplay = "+2 Credits",
        isPositive = true,
        title = "Welcome credits",
        timestamp = "Today"
    ),
    CreditHistoryItem(
        id = "tx_2",
        amountDisplay = "−1 Credit",
        isPositive = false,
        title = "Try-On",
        timestamp = "Yesterday"
    ),
    CreditHistoryItem(
        id = "tx_3",
        amountDisplay = "+1 Credit",
        isPositive = true,
        title = "Try-On refund",
        timestamp = "Yesterday"
    ),
    CreditHistoryItem(
        id = "tx_4",
        amountDisplay = "+5 Credits",
        isPositive = true,
        title = "Credit purchase",
        timestamp = "Earlier"
    )
)

/**
 * Subtle tactile bounce modifier for button press feedback.
 */
@Composable
fun Modifier.bounceScale(interactionSource: InteractionSource): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Backward-compatible entry point that displays the production CreditsScreen.
 */
@Composable
fun CreditStoreScreen(navController: NavController) {
    CreditsScreen(navController = navController)
}

/**
 * Production-quality Credits Screen for TiHin.
 *
 * Provides:
 * 1. Compact Credit Balance Hero (Free vs Purchased breakdown and ad-disclosure)
 * 2. Buy Credits Section (₹49 and ₹99 packs with ad-free messaging)
 * 3. TiHin Premium Section (₹299/month tier with full benefits breakdown)
 * 4. Credit History (Clean scannable transaction list with positive green accent)
 * 5. Dynamic support for low/empty balances, free-only, purchased-only, and combined balances
 */
@Composable
fun CreditsScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val freeCredits = SessionManager.freeCredits
    val purchasedCredits = SessionManager.purchasedCredits
    val totalCredits = SessionManager.credits

    // Dynamic supporting copy based on balance state
    val supportingText = when {
        freeCredits > 0 && purchasedCredits > 0 -> "$freeCredits Free · $purchasedCredits Purchased"
        freeCredits > 0 && purchasedCredits == 0 -> "$freeCredits Free ${if (freeCredits == 1) "Credit" else "Credits"}"
        freeCredits == 0 && purchasedCredits > 0 -> "$purchasedCredits Purchased ${if (purchasedCredits == 1) "Credit" else "Credits"}"
        else -> "0 Credits remaining"
    }

    // Dynamic explanatory copy
    val explanatoryText = when {
        freeCredits > 0 && purchasedCredits > 0 -> "Purchased credits are ad-free. Free Try-On includes a short ad."
        purchasedCredits > 0 -> "Purchased credits are ad-free."
        freeCredits > 0 -> "Free Try-On includes a short ad."
        else -> "Free Try-On includes a short ad. Buy credits below for an ad-free experience."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("credits_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("credits_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryText
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Credits",
                style = Typography.displaySmall.copy(fontSize = 24.sp),
                color = PrimaryText,
                fontWeight = FontWeight.Bold
            )
        }

        // Scrollable Body Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. CREDIT BALANCE HERO
            CreditBalanceHero(
                totalCredits = totalCredits,
                supportingText = supportingText,
                explanatoryText = explanatoryText
            )

            // 2. BUY CREDITS SECTION
            BuyCreditsSection(
                packs = DEFAULT_CREDIT_PACKS,
                onBuyPack = { pack ->
                    // Prototype interaction: adds purchased credits to local session
                    SessionManager.addPurchasedCredits(pack.credits)
                    /* Toast disabled for tests */
                }
            )

            // 3. TIHIN PREMIUM SECTION
            PremiumPlanCard(
                plan = PremiumPlan(),
                onGetPremiumClicked = {
                    /* Toast disabled for tests */
                }
            )

            // 4. CREDIT HISTORY SECTION
            CreditHistorySection(
                historyItems = MOCK_CREDIT_HISTORY,
                onViewAllClicked = {
                    /* Toast disabled for tests */
                }
            )

            // 5. PRODUCT RULES / HOW CREDITS WORK NOTE
            CreditsRulesNote()
        }
    }
}

/**
 * Compact, fashion-editorial Hero card showing current credit balance.
 */
@Composable
fun CreditBalanceHero(
    totalCredits: Int,
    supportingText: String,
    explanatoryText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("credit_balance_hero"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = "Your Credits",
                style = Typography.labelMedium.copy(
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✦",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChampagneGold
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$totalCredits",
                    style = Typography.displayLarge.copy(
                        fontFamily = CormorantGaramond,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimaryText
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = supportingText,
                style = Typography.bodyMedium.copy(
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (totalCredits > 0) DeepForest else SecondaryText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = explanatoryText,
                style = Typography.bodySmall.copy(
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = TertiaryText
            )
        }
    }
}

/**
 * Buy Credits Section displaying refined individual credit packs.
 */
@Composable
fun BuyCreditsSection(
    packs: List<CreditPack>,
    onBuyPack: (CreditPack) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("buy_credits_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Buy Credits",
                style = Typography.headlineSmall.copy(
                    fontFamily = Inter,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Purchased credits are ad-free.",
                style = Typography.bodySmall.copy(
                    fontFamily = Inter,
                    fontSize = 13.sp
                ),
                color = SecondaryText
            )
        }

        packs.forEach { pack ->
            CreditPackCard(
                pack = pack,
                onBuyClicked = { onBuyPack(pack) }
            )
        }
    }
}

/**
 * Clean, premium purchase card for an individual credit pack.
 */
@Composable
fun CreditPackCard(
    pack: CreditPack,
    onBuyClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("credit_pack_card_${pack.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(
            width = if (pack.isPopular) 1.5.dp else 1.dp,
            color = if (pack.isPopular) ChampagneGold else BorderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (pack.isPopular) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ChampagneGold.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(
                        text = "Most Popular",
                        style = Typography.labelSmall.copy(
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = DeepForest,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${pack.credits} Credits",
                        style = Typography.titleMedium.copy(
                            fontFamily = Inter,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PrimaryText
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(DeepForest)
                        )
                        Text(
                            text = pack.featureTag,
                            style = Typography.bodySmall.copy(
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = DeepForest
                        )
                    }
                }

                Text(
                    text = pack.priceDisplay,
                    style = Typography.headlineMedium.copy(
                        fontFamily = Inter,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimaryText
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onBuyClicked,
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .bounceScale(interactionSource)
                    .testTag("buy_button_${pack.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurchaseCTA,
                    contentColor = CTAText
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text(
                    text = pack.buttonText,
                    style = Typography.labelLarge.copy(
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = CTAText
                )
            }
        }
    }
}

/**
 * Restrained, visually premium card presenting the TiHin Premium subscription option.
 */
@Composable
fun PremiumPlanCard(
    plan: PremiumPlan,
    onGetPremiumClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tihin_premium_section"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Title & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ChampagneGold.copy(alpha = 0.12f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "PREMIUM TIER",
                            style = Typography.labelSmall.copy(
                                fontFamily = Inter,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = ChampagneGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = plan.title,
                        style = Typography.headlineMedium.copy(
                            fontFamily = CormorantGaramond,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PrimaryText
                    )
                    Text(
                        text = plan.subtitle,
                        style = Typography.bodySmall.copy(
                            fontFamily = Inter,
                            fontSize = 13.sp
                        ),
                        color = SecondaryText
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = plan.priceDisplay,
                            style = Typography.headlineMedium.copy(
                                fontFamily = Inter,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = PrimaryText
                        )
                        Text(
                            text = plan.periodDisplay,
                            style = Typography.bodySmall.copy(
                                fontFamily = Inter,
                                fontSize = 12.sp
                            ),
                            color = SecondaryText,
                            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.6f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Benefits Checklist
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                plan.benefits.forEach { benefit ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(DeepForest.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = DeepForest,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = benefit,
                            style = Typography.bodyMedium.copy(
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = PrimaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary CTA
            Button(
                onClick = onGetPremiumClicked,
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .bounceScale(interactionSource)
                    .testTag("get_premium_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurchaseCTA,
                    contentColor = CTAText
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text(
                    text = plan.ctaText,
                    style = Typography.labelLarge.copy(
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = CTAText
                )
            }
        }
    }
}

/**
 * Credit History Section displaying a compact recent transaction list.
 */
@Composable
fun CreditHistorySection(
    historyItems: List<CreditHistoryItem>,
    onViewAllClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("credit_history_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Credit History",
                style = Typography.headlineSmall.copy(
                    fontFamily = Inter,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = PrimaryText
            )
            Text(
                text = "View all →",
                style = Typography.labelMedium.copy(
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = DeepForest,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onViewAllClicked() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                historyItems.forEachIndexed { index, item ->
                    CreditHistoryRow(item = item)
                    if (index < historyItems.size - 1) {
                        HorizontalDivider(
                            color = BorderColor.copy(alpha = 0.5f),
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual row within Credit History.
 */
@Composable
fun CreditHistoryRow(
    item: CreditHistoryItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.title,
                style = Typography.bodyMedium.copy(
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = PrimaryText
            )
            Text(
                text = item.timestamp,
                style = Typography.bodySmall.copy(
                    fontFamily = Inter,
                    fontSize = 12.sp
                ),
                color = TertiaryText
            )
        }

        Text(
            text = item.amountDisplay,
            style = Typography.bodyMedium.copy(
                fontFamily = Inter,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            color = if (item.isPositive) DeepForest else PrimaryText
        )
    }
}

/**
 * Informative note at bottom explaining TiHin credit policies cleanly.
 */
@Composable
fun CreditsRulesNote(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantColor),
        border = BorderStroke(0.8.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "How TiHin Credits Work",
                style = Typography.labelMedium.copy(
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = PrimaryText
            )
            Text(
                text = "• 1 successful Try-On consumes 1 credit. Failed or cancelled attempts return the held credit.",
                style = Typography.bodySmall.copy(
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = SecondaryText
            )
            Text(
                text = "• Free Try-Ons include a short ad. Watching an ad does not add extra credits.",
                style = Typography.bodySmall.copy(
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = SecondaryText
            )
            Text(
                text = "• Purchased credits and Premium subscriptions are 100% ad-free.",
                style = Typography.bodySmall.copy(
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = SecondaryText
            )
            Text(
                text = "• Price Tracking, Favourites, Saving Looks, and Shopping never consume credits.",
                style = Typography.bodySmall.copy(
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = SecondaryText
            )
        }
    }
}
