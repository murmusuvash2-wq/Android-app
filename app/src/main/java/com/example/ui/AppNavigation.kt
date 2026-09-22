package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepForest
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.SurfaceVariantColor
import com.example.ui.theme.WarmIvory

enum class BottomTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "bottom_tab_home"),
    LOOKS("Looks", Icons.Filled.Collections, Icons.Outlined.Collections, "bottom_tab_looks"),
    DISCOVER("Discover", Icons.Filled.Explore, Icons.Outlined.Explore, "bottom_tab_discover"),
    ME("Me", Icons.Filled.Person, Icons.Outlined.Person, "bottom_tab_me")
}

@Composable
fun AppNavigation(
    startDestination: String = Screen.Splash.route,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize().background(WarmIvory),
        enterTransition = {
            fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.985f, animationSpec = tween(220, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(160, easing = FastOutLinearInEasing))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(160, easing = FastOutLinearInEasing)) +
                    scaleOut(targetScale = 0.985f, animationSpec = tween(160, easing = FastOutLinearInEasing))
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            AppShell(rootNavController = navController, initialTab = 0)
        }
        composable(Screen.Looks.route) {
            AppShell(rootNavController = navController, initialTab = 1)
        }
        composable(Screen.Discover.route) {
            AppShell(rootNavController = navController, initialTab = 2)
        }
        composable(Screen.Me.route) {
            AppShell(rootNavController = navController, initialTab = 3)
        }
        composable(
            route = Screen.TryOn.route,
            enterTransition = {
                fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(240, easing = FastOutSlowInEasing)
                        ) { (it * 0.08f).toInt() }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing)) +
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(200, easing = FastOutLinearInEasing)
                        ) { (it * 0.08f).toInt() }
            }
        ) {
            TryOnScreen(navController = navController)
        }
        composable(
            route = Screen.Processing.route,
            enterTransition = {
                fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.97f, animationSpec = tween(240, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
            }
        ) {
            ProcessingScreen(navController = navController)
        }
        composable(
            route = Screen.Result.route,
            enterTransition = {
                fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.97f, animationSpec = tween(280, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing)) +
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(220, easing = FastOutLinearInEasing)
                        ) { (it * 0.08f).toInt() }
            }
        ) {
            ResultScreen(navController = navController)
        }
        composable(
            route = Screen.CreditStore.route,
            enterTransition = {
                fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(240, easing = FastOutSlowInEasing)
                        ) { (it * 0.08f).toInt() }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing)) +
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(200, easing = FastOutLinearInEasing)
                        ) { (it * 0.08f).toInt() }
            }
        ) {
            CreditStoreScreen(navController = navController)
        }
        composable(Screen.SharedProductError.route) {
            SharedProductErrorScreen(navController = navController)
        }
    }
}

@Composable
fun AppShell(
    rootNavController: NavHostController,
    initialTab: Int = 0
) {
    var currentTab by rememberSaveable { mutableIntStateOf(initialTab) }
    var looksSubTab by rememberSaveable { mutableIntStateOf(0) }

    // Enforce back button hierarchy: return to Home tab if on another tab
    BackHandler(enabled = currentTab != 0) {
        currentTab = 0
    }

    Scaffold(
        containerColor = WarmIvory,
        bottomBar = {
            NavigationBar(
                containerColor = WarmIvory,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .testTag("app_bottom_bar")
                    .drawBehind {
                        drawLine(
                            color = CardBorder,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                BottomTab.entries.forEachIndexed { index, tab ->
                    val isSelected = currentTab == index
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "nav_icon_scale_${tab.name}"
                    )
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepForest,
                            selectedTextColor = DeepForest,
                            unselectedIconColor = SecondaryText,
                            unselectedTextColor = SecondaryText,
                            indicatorColor = DeepForest.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier
                            .testTag(tab.testTag)
                            .height(60.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) togetherWith
                            fadeOut(animationSpec = tween(140, easing = FastOutLinearInEasing))
                },
                label = "app_shell_tab_content"
            ) { tab ->
                when (tab) {
                    0 -> HomeScreen(
                        navController = rootNavController,
                        onNavigateToDiscover = {
                            currentTab = 2
                        }
                    )
                    1 -> LooksScreen(
                        navController = rootNavController,
                        initialTab = looksSubTab
                    )
                    2 -> DiscoverScreen(navController = rootNavController)
                    3 -> MeScreen(
                        navController = rootNavController,
                        onNavigateToLooksTab = { tabIdx ->
                            looksSubTab = tabIdx
                            currentTab = 1
                        }
                    )
                }
            }
        }
    }
}
