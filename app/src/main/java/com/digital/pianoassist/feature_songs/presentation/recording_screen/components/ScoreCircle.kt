package com.digital.pianoassist.feature_songs.presentation.recording_screen.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ScoreCircle(score: Double) {
    var oldScore by remember { mutableStateOf(score) }

    // Animate the stroke width when the score changes
    val animatedStrokeWidth by animateFloatAsState(
        targetValue = if (oldScore != score) 7f else 0f,
        animationSpec = tween(durationMillis = 600), label = ""
    )

    // Update oldScore whenever the score changes
    LaunchedEffect(score) {
        delay(600)
        oldScore = score
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(80.dp)
            .drawBehind {
                val radius = 30.dp.toPx() - animatedStrokeWidth / 2

                // Draw the main circle
                drawCircle(
                    color = Color(0xFF8A9A5B),
                    radius = radius
                )

                // Draw the animated gold edge
                if (animatedStrokeWidth > 0) {
                    drawCircle(
                        color = Color(0xFFffd700),
                        radius = radius,
                        style = Stroke(width = animatedStrokeWidth)
                    )
                }
            }
    ) {
        Text(
            text = score.toString(),
            fontSize = 17.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )
    }
}