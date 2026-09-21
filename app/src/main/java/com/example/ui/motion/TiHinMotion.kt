package com.example.ui.motion

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.Charcoal
import com.example.ui.theme.DeepForest
import com.example.ui.theme.Inter
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SubtleCardElevation
import com.example.ui.theme.SurfaceColor

/**
 * TiHin Motion System:
 * Pure, calm, luxury fashion animations adhering strictly to:
 * - Subtle, purposeful, and fast (100–250ms)
 * - Zero flashy bouncing, zero neon effects, zero particle loops
 * - Micro-interactions scale down to 0.96 on press and restore with restrained spring
 */
object TiHinMotion {
    const val DURATION_PRESS = 100
    const val DURATION_FAST = 150
    const val DURATION_STANDARD = 220
    const val DURATION_SETTLE = 280

    // Calm decelerating easing curve for luxury motion
    val LuxuryDecelerate = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val LuxuryAccelerate = FastOutLinearInEasing

    // Restrained spring for button press restore
    val RestrainedRestoreSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // Screen Transitions for NavHost
    fun defaultEnterTransition(): EnterTransition {
        return fadeIn(animationSpec = tween(DURATION_STANDARD, easing = LuxuryDecelerate)) +
                scaleIn(initialScale = 0.985f, animationSpec = tween(DURATION_STANDARD, easing = LuxuryDecelerate))
    }

    fun defaultExitTransition(): ExitTransition {
        return fadeOut(animationSpec = tween(DURATION_FAST, easing = LuxuryAccelerate))
    }

    fun defaultPopEnterTransition(): EnterTransition {
        return fadeIn(animationSpec = tween(DURATION_STANDARD, easing = LuxuryDecelerate))
    }

    fun defaultPopExitTransition(): ExitTransition {
        return fadeOut(animationSpec = tween(DURATION_FAST, easing = LuxuryAccelerate))
    }
}

/**
 * Button Press Micro-interaction Modifier:
 * Scales down to 0.96 on press, quickly and smoothly springs back on release (~100–200ms).
 * Does not alter click handling or button behavior.
 */
fun Modifier.tihinButtonPress(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1.0f,
        animationSpec = if (isPressed) {
            tween(durationMillis = TiHinMotion.DURATION_PRESS, easing = FastOutSlowInEasing)
        } else {
            TiHinMotion.RestrainedRestoreSpring
        },
        label = "tihin_button_press_scale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Favourite / Heart Icon with gentle micro-pop animation:
 * Animates 1.0f -> 1.22f -> 1.0f smoothly when favorited.
 * Pure UI animation only; data state remains untouched.
 */
@Composable
fun TiHinAnimatedHeartIcon(
    isFavourite: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    activeTint: Color = DeepForest,
    inactiveTint: Color = PrimaryText,
    iconSize: Dp = 18.dp
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isFavourite) {
        if (isFavourite) {
            scale.animateTo(
                targetValue = 1.22f,
                animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        } else {
            scale.snapTo(1f)
        }
    }

    Icon(
        imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
        contentDescription = contentDescription,
        tint = if (isFavourite) activeTint else inactiveTint,
        modifier = modifier
            .size(iconSize)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
    )
}

/**
 * Animated Credits Pill:
 * Subtle Champagne Gold pulse and vertical slide-fade transition when credit value changes.
 * Pure UI presentation component.
 */
@Composable
fun TiHinAnimatedCreditsPill(
    credits: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = if (isPressed) {
            tween(durationMillis = TiHinMotion.DURATION_PRESS, easing = FastOutSlowInEasing)
        } else {
            TiHinMotion.RestrainedRestoreSpring
        },
        label = "tihin_pill_press"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        color = SurfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        shadowElevation = SubtleCardElevation,
        modifier = modifier
            .testTag("tries_counter_pill")
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Credits",
                tint = ChampagneGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            AnimatedContent(
                targetState = credits,
                transitionSpec = {
                    (slideInVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) { -it / 3 } +
                            fadeIn(animationSpec = tween(180)))
                        .togetherWith(
                            slideOutVertically(animationSpec = tween(140, easing = FastOutLinearInEasing)) { it / 3 } +
                                    fadeOut(animationSpec = tween(140))
                        )
                },
                label = "tihin_credits_counter_text"
            ) { creditCount ->
                Text(
                    text = "$creditCount Credits",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal
                )
            }
        }
    }
}
