package com.abulubad.sojourn.core.designsystem.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion

/** Spring stiffness for anything dragged or popped (docs/design-system/motion.md). */
private const val SPRING_STIFFNESS = 400f

/** Damping for things that settle: sheets, gallery pages. */
private const val SETTLE_DAMPING = 0.8f

/** Damping for things that pop: the save heart, the selected pin. */
private const val POP_DAMPING = 0.6f

/** Exits run at two thirds of the enter duration ("enter soft, leave fast"). */
private const val EXIT_RATIO_NUMERATOR = 2
private const val EXIT_RATIO_DENOMINATOR = 3

/** Settles sheets, gallery pages and back gestures. */
fun <T> SojournMotion.settleSpring(): SpringSpec<T> = spring(
    dampingRatio = SETTLE_DAMPING,
    stiffness = SPRING_STIFFNESS,
)

/** Pops the save heart and the selected price pin. */
fun <T> SojournMotion.popSpring(): SpringSpec<T> = spring(dampingRatio = POP_DAMPING, stiffness = SPRING_STIFFNESS)

/** Exits run at two thirds of their enter duration. */
fun SojournMotion.exitDuration(enterMillis: Int): Int = enterMillis * EXIT_RATIO_NUMERATOR / EXIT_RATIO_DENOMINATOR

/**
 * An entrance: [durationMillis] with `emphasized` easing — or, when reduce motion is on, a `fast` fade-length
 * tween with `standard` easing so the caller can still crossfade (M-03).
 */
@Composable
@ReadOnlyComposable
fun <T> SojournMotion.enter(durationMillis: Int): FiniteAnimationSpec<T> =
    if (LocalReduceMotion.current) tween(fast, easing = standard) else tween(durationMillis, easing = emphasized)

/** An exit: two thirds of [enterMillis] with `exit` easing, or the reduce-motion fade. */
@Composable
@ReadOnlyComposable
fun <T> SojournMotion.exit(enterMillis: Int): FiniteAnimationSpec<T> =
    if (LocalReduceMotion.current) tween(fast, easing = standard) else tween(exitDuration(enterMillis), easing = exit)

/** Something that moves and stays on screen: [durationMillis] with `standard` easing. */
@Composable
@ReadOnlyComposable
fun <T> SojournMotion.move(durationMillis: Int): FiniteAnimationSpec<T> =
    tween(if (LocalReduceMotion.current) fast else durationMillis, easing = standard)
