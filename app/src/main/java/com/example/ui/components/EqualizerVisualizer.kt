package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldLight

@Composable
fun EqualizerVisualizer(
    amplitudes: List<Float>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barWidth: Dp = 3.5.dp,
    maxHeight: Dp = 48.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        amplitudes.forEachIndexed { index, amp ->
            val targetHeightFraction = if (isPlaying) amp.coerceIn(0.12f, 1.0f) else 0.15f
            val animatedFraction by animateFloatAsState(
                targetValue = targetHeightFraction,
                animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing),
                label = "eq_bar_$index"
            )

            val colorBrush = Brush.verticalGradient(
                colors = listOf(
                    CyanLight,
                    CyanPrimary,
                    if (index % 2 == 0) BlueAccent else EmeraldLight
                )
            )

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(animatedFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colorBrush)
            )
        }
    }
}

