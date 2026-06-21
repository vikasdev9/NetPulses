package com.example.netpulse.ui.components

import androidx.compose.ui.res.stringResource
import com.example.netpulse.R
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NetworkBadge(
    networkType: String = "WiFi",
    isConnected: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(99.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Wifi,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$networkType · ${if (isConnected) stringResource(R.string.network_connected) else stringResource(R.string.network_disconnected)}",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp
        )
        if (isConnected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
            )
        }
    }
}
