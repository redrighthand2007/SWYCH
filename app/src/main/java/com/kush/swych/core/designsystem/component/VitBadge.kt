package com.kush.swych.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val GoldLight = Color(0xFFFFE082)
private val Gold = Color(0xFFFFB300)
private val GoldDeep = Color(0xFFC77800)
private val Ink = Color(0xFF3E2500)

/**
 * Golden "V": the account confirmed a @vitstudent.ac.in email.
 * Drawn as shapes rather than text, so it stays sharp and centred at any size.
 */
@Composable
fun VitBadge(modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Verified VIT student" }
    ) {
        val r = this.size.minDimension / 2
        // Gold coin with a darker rim
        drawCircle(GoldDeep, radius = r)
        drawCircle(
            brush = Brush.linearGradient(
                listOf(GoldLight, Gold),
                start = Offset.Zero,
                end = Offset(this.size.width, this.size.height)
            ),
            radius = r * 0.86f
        )
        // Bold "V"
        val w = this.size.width
        val h = this.size.height
        val v = Path().apply {
            moveTo(w * 0.30f, h * 0.32f)
            lineTo(w * 0.50f, h * 0.70f)
            lineTo(w * 0.70f, h * 0.32f)
        }
        drawPath(
            v,
            color = Ink,
            style = Stroke(width = w * 0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
