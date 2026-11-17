package com.abulubad.sojourn.core.designsystem.components.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.abulubad.sojourn.core.designsystem.theme.LocalReduceMotion
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion

/**
 * The save pop (motion.md): when [saved] turns on, the heart dips to 0.8, pops to 1.3 and settles over `base`.
 * Turning it off, the first composition and reduce motion do not animate. Read the scale while drawing.
 */
@Composable
internal fun rememberSavePopScale(saved: Boolean): () -> Float {
    val scale = remember { Animatable(1f) }
    var previous by remember { mutableStateOf(saved) }
    val reduceMotion = LocalReduceMotion.current
    LaunchedEffect(saved) {
        if (saved && !previous && !reduceMotion) {
            scale.snapTo(1f)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = SojournMotion.base
                    SAVE_DIP at SojournMotion.base * DIP_AT_PERCENT / PERCENT
                    SAVE_POP at SojournMotion.base * POP_AT_PERCENT / PERCENT
                },
            )
        }
        previous = saved
    }
    return { scale.value }
}

private const val SAVE_DIP = 0.8f
private const val SAVE_POP = 1.3f
private const val DIP_AT_PERCENT = 30
private const val POP_AT_PERCENT = 65
private const val PERCENT = 100
