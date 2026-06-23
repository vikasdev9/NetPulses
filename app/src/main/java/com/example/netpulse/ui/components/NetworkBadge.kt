package com.example.netpulse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cable
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiOff
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
import com.example.netpulse.utils.NetworkIcon
import com.example.netpulse.utils.NetworkState

@Composable
fun NetworkBadge(
    networkState: NetworkState
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    val dotColor by animateColorAsState(
        targetValue = if (networkState.isConnected)
            MaterialTheme.colorScheme.tertiary
        else
            MaterialTheme.colorScheme.error,
        animationSpec = tween(300),
        label = "dotColor"
    )

    val networkIcon = when (networkState.signalIcon) {
        NetworkIcon.WIFI -> Icons.Outlined.Wifi
        NetworkIcon.MOBILE_5G,
        NetworkIcon.MOBILE_4G,
        NetworkIcon.MOBILE_3G,
        NetworkIcon.MOBILE_2G -> Icons.Outlined.SignalCellularAlt
        NetworkIcon.ETHERNET -> Icons.Outlined.Cable
        NetworkIcon.NONE -> Icons.Outlined.WifiOff
    }

    val badgeText = if (networkState.isConnected) {
        "${networkState.networkType} · Connected"
    } else {
        "No Connection"
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                0.5.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(99.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = networkIcon,
            contentDescription = null,
            tint = if (networkState.isConnected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = badgeText,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp
        )

        Box(
            modifier = Modifier
                .size(7.dp)
                .scale(if (networkState.isConnected) dotScale else 1f)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}
