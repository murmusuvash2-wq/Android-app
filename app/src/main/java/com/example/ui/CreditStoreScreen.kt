package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DeepForest
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PurchaseCTA
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.SurfaceVariantColor
import com.example.ui.theme.WarmIvory

data class CreditPackage(
    val title: String,
    val credits: Int,
    val price: String,
    val isPopular: Boolean = false,
    val description: String
)

@Composable
fun CreditStoreScreen(navController: NavController) {
    val context = LocalContext.current
    val currentCredits = SessionManager.credits

    val packages = listOf(
        CreditPackage(
            title = "Starter Pack",
            credits = 10,
            price = "₹199",
            isPopular = false,
            description = "Perfect for trying on a weekend wardrobe refresh."
        ),
        CreditPackage(
            title = "Style Enthusiast",
            credits = 50,
            price = "₹499",
            isPopular = true,
            description = "Best value. Try on full collections across all merchants."
        ),
        CreditPackage(
            title = "Unlimited Monthly",
            credits = 200,
            price = "₹999",
            isPopular = false,
            description = "Unlimited virtual fittings for serious fashion shoppers."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
            .testTag("credit_store_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
            }
            Text(
                text = "Credit Store",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }

        // Current Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Balance",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SecondaryText
                    )
                    Text(
                        text = "$currentCredits Try-On Credits",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ChampagneGold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = DeepForest,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select a Package",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Packages
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            packages.forEach { pkg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            SessionManager.addCredits(pkg.credits)
                            Toast.makeText(context, "Added ${pkg.credits} credits to your account!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    border = BorderStroke(
                        if (pkg.isPopular) 1.5.dp else 1.dp,
                        if (pkg.isPopular) ChampagneGold else BorderColor
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        if (pkg.isPopular) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ChampagneGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "MOST POPULAR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = pkg.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                                Text(
                                    text = "${pkg.credits} Try-Ons",
                                    fontSize = 13.sp,
                                    color = DeepForest,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = pkg.price,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = pkg.description,
                            fontSize = 12.sp,
                            color = SecondaryText,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
