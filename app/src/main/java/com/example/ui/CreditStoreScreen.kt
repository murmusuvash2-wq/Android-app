package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.*

@Composable
fun CreditStoreScreen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Charcoal)
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(ChampagneGold.copy(alpha = 0.12f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ChampagneGold,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Get More Tries", color = Charcoal, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Unlock high-precision AI Try-Ons, HD downloads, and watermark-free results.",
                color = SoftCharcoal,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            CreditPackageCard(
                title = "10 Try-Ons",
                price = "₹199",
                subtitle = "Great for a quick style check",
                isPopular = false,
                onClick = {
                    SessionManager.credits += 10
                    Toast.makeText(context, "10 Try-Ons added! (${SessionManager.credits} total)", Toast.LENGTH_SHORT).show()
                    navController.navigateUp()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            CreditPackageCard(
                title = "50 Try-Ons",
                price = "₹499",
                subtitle = "Best value for wardrobe planning",
                isPopular = true,
                onClick = {
                    SessionManager.credits += 50
                    Toast.makeText(context, "50 Try-Ons added! (${SessionManager.credits} total)", Toast.LENGTH_SHORT).show()
                    navController.navigateUp()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            CreditPackageCard(
                title = "Unlimited (Monthly)",
                price = "₹999",
                subtitle = "Unlimited tries, 4K render & no ads",
                isPopular = false,
                onClick = {
                    SessionManager.credits += 999
                    Toast.makeText(context, "Unlimited subscription active!", Toast.LENGTH_SHORT).show()
                    navController.navigateUp()
                }
            )
        }
    }
}

@Composable
fun CreditPackageCard(
    title: String,
    price: String,
    subtitle: String,
    isPopular: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isPopular) SurfaceVariantColor else White, RoundedCornerShape(20.dp))
            .border(if (isPopular) 1.5.dp else 1.dp, if (isPopular) ChampagneGold else WarmGray, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (isPopular) {
                    Text(
                        text = "MOST POPULAR",
                        color = ChampagneGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(title, color = Charcoal, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = SoftCharcoal, fontSize = 12.sp)
            }
            Text(price, color = Charcoal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
