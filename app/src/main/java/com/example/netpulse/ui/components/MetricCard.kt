package com.example.netpulse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.ui.theme.*

@Composable
fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String = "",
    accentColor: Color,
    iconBgColor: Color,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) accentColor else MaterialTheme.colorScheme.outline,
        animationSpec = tween(300),
        label = "borderColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .shadow(
                elevation = if (isActive) 10.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = accentColor,
                ambientColor = accentColor
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isActive) {
                    LiveBadge()
                }
            }

            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MonospaceFontFamily
                    )
                    if (unit.isNotEmpty() && value != "—") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun LiveBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "LIVE",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
