package com.kush.swych.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GoldLight = Color(0xFFFFD54F)
private val GoldDark = Color(0xFFE6A100)
private val GoldText = Color(0xFF5A3A00)

/** Golden "V": the account confirmed a @vitstudent.ac.in email. */
@Composable
fun VitBadge(modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(Brush.linearGradient(listOf(GoldLight, GoldDark)), CircleShape)
            .semantics { contentDescription = "Verified VIT student" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "V",
            color = GoldText,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.6f).sp
        )
    }
}
