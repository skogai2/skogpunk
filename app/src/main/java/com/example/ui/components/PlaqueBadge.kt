package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SkCard
import com.example.ui.theme.SkFg
import com.example.ui.theme.SkFgMuted
import com.example.ui.theme.SkNeonCyan
import com.example.ui.theme.SkNeonViolet

enum class PlaqueStyle(val accentColor: Color) {
    VIOLET(SkNeonViolet),
    CYAN(SkNeonCyan)
}

@Composable
fun PlaqueBadge(
    label: String,
    value: String,
    sublabel: String? = null,
    style: PlaqueStyle = PlaqueStyle.VIOLET,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, style.accentColor.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
            .background(SkCard, RoundedCornerShape(4.dp))
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = label.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                color = SkFgMuted
            )
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = style.accentColor
            )
            if (sublabel != null) {
                Text(
                    text = sublabel.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = SkFg
                )
            }
        }
    }
}
