package com.kush.swych.core.designsystem.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

/** Every screen and tab switch is a plain crossfade of this length. */
const val SCREEN_FADE_MILLIS = 500

val screenFadeIn: EnterTransition = fadeIn(animationSpec = tween(SCREEN_FADE_MILLIS))
val screenFadeOut: ExitTransition = fadeOut(animationSpec = tween(SCREEN_FADE_MILLIS))

fun screenCrossfade(): ContentTransform = screenFadeIn togetherWith screenFadeOut
