package com.example.netpulse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.ui.theme.Background
import com.example.netpulse.ui.theme.CardBorder
import com.example.netpulse.ui.theme.PrimaryAccent
import com.example.netpulse.ui.theme.TextSecondary

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .background(Background, RoundedCornerShape(8.dp))
            .padding(1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = selectedIndex == index
            
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) PrimaryAccent else Color.Transparent,
                animationSpec = tween(300),
                label = "bgColor"
            )
            
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else TextSecondary,
                animationSpec = tween(300),
                label = "textColor"
            )
            
            Box(
                modifier = Modifier
                    .widthIn(min = 36.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(backgroundColor)
                    .clickable { onSelectionChanged(index) }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
