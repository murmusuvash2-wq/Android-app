package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun AppShell(rootNavController: androidx.navigation.NavController) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var looksSubTab by rememberSaveable(stateSaver = LooksTabSaver) { mutableStateOf(LooksTab.RECENT) }

    // AppShell-level back handling: non-Home tab returns to Home tab.
    // When on Home (tab 0), BackHandler is disabled, preserving normal system back behavior.
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        containerColor = WarmIvory,
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = { AppBottomNavigation(selectedTab, rootNavController) { selectedTab = it } }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen(
                    navController = rootNavController,
                    onNavigateToDiscover = { selectedTab = 2 }
                )
                1 -> LooksScreen(
                    navController = rootNavController,
                    requestedTab = looksSubTab,
                    onTabChanged = { looksSubTab = it }
                )
                2 -> DiscoverScreen(rootNavController)
                3 -> MeScreen(
                    navController = rootNavController,
                    onNavigateToLooks = { subTab ->
                        looksSubTab = subTab
                        selectedTab = 1
                    }
                )
            }
        }
    }
}

@Composable
fun AppBottomNavigation(selectedTab: Int, rootNavController: androidx.navigation.NavController, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = OffWhite,
        tonalElevation = 8.dp,
        windowInsets = NavigationBarDefaults.windowInsets,
        modifier = Modifier
            .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DeepForest,
                selectedTextColor = DeepForest,
                indicatorColor = DeepForest.copy(alpha = 0.12f),
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
                selectedIconColor = DeepForest,
                selectedTextColor = DeepForest,
                indicatorColor = DeepForest.copy(alpha = 0.12f),
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
                selectedIconColor = DeepForest,
                selectedTextColor = DeepForest,
                indicatorColor = DeepForest.copy(alpha = 0.12f),
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
                selectedIconColor = DeepForest,
                selectedTextColor = DeepForest,
                indicatorColor = DeepForest.copy(alpha = 0.12f),
                unselectedIconColor = SoftCharcoal,
                unselectedTextColor = SoftCharcoal
            )
        )
    }
}
