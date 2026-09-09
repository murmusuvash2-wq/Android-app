package com.example.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized Spacing System for consistent spatial rhythm and balanced density.
 */
object AppSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val section: Dp = 28.dp
    val majorSection: Dp = 32.dp

    // Default horizontal screen padding
    val screenHorizontal: Dp = 16.dp
    val screenHorizontalWide: Dp = 20.dp
}

// Convenient top-level spacing tokens
val SpacingXs = AppSpacing.xs
val SpacingSm = AppSpacing.sm
val SpacingMd = AppSpacing.md
val SpacingLg = AppSpacing.lg
val SpacingXl = AppSpacing.xl
val SpacingXxl = AppSpacing.xxl
val SpacingSection = AppSpacing.section
val SpacingMajorSection = AppSpacing.majorSection
val ScreenPaddingHorizontal = AppSpacing.screenHorizontal
