package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Cabinet
import com.example.ui.theme.SkBg
import com.example.ui.theme.SkFg

@Composable
fun CabinetStage(
    cabinet: Cabinet,
    stageTitle: String? = null,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val stageHeight = if (isCompact) 120.dp else 180.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(stageHeight)
            .border(1.dp, cabinet.borderColor.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(SkBg)
    ) {
        // Pixel scene image
        Image(
            painter = painterResource(id = cabinet.drawableRes),
            contentDescription = "${cabinet.title} Scene",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x880B0716),
                            Color(0xDD0B0716)
                        )
                    )
                )
        )

        // Cabinet marquee tag at bottom
        if (stageTitle != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color(0xCC1A1030), RoundedCornerShape(2.dp))
                    .border(1.dp, cabinet.primaryColor.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stageTitle.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = SkFg
                )
            }
        }
    }
}
