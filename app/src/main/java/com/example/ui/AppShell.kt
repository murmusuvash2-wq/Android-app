package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun AppShell(rootNavController: androidx.navigation.NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    Scaffold(
        containerColor = WarmIvory,
        bottomBar = { AppBottomNavigation(selectedTab, rootNavController) { selectedTab = it } }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen(rootNavController)
                1 -> LooksScreen(rootNavController)
                2 -> DiscoverScreen(rootNavController)
                3 -> MeScreen(rootNavController)
            }
        }
    }
}

@Composable
fun AppBottomNavigation(selectedTab: Int, rootNavController: androidx.navigation.NavController, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = OffWhite,
        tonalElevation = 8.dp,
        modifier = Modifier
            .height(72.dp)
            .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Lavender,
                selectedTextColor = Lavender,
                indicatorColor = SoftLavender,
                unselectedIconColor = SoftCharcoal,
                unselectedTextColor = SoftCharcoal
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.Checkroom, contentDescription = "Looks") },
            label = { Text("Looks", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Lavender,
                selectedTextColor = Lavender,
                indicatorColor = SoftLavender,
                unselectedIconColor = SoftCharcoal,
                unselectedTextColor = SoftCharcoal
            )
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Discover") },
            label = { Text("Discover", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Lavender,
                selectedTextColor = Lavender,
                indicatorColor = SoftLavender,
                unselectedIconColor = SoftCharcoal,
                unselectedTextColor = SoftCharcoal
            )
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Me") },
            label = { Text("Me", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Lavender,
                selectedTextColor = Lavender,
                indicatorColor = SoftLavender,
                unselectedIconColor = SoftCharcoal,
                unselectedTextColor = SoftCharcoal
            )
        )
    }
}
