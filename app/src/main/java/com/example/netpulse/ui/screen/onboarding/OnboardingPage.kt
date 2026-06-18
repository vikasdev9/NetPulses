package com.example.netpulse.ui.screen.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingPage(
    item: OnboardingItem,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ILLUSTRATION AREA
        Box(
            modifier = Modifier
                .size(if (isTablet) 320.dp else 240.dp)
                .clip(CircleShape)
                .background(item.highlightColor.copy(alpha = 0.1f))
                .border(1.dp, item.highlightColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Background Blur Glow
            Box(
                modifier = Modifier
                    .size(if (isTablet) 180.dp else 120.dp)
                    .blur(60.dp)
                    .background(item.highlightColor.copy(alpha = 0.3f))
            )

            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(if (isTablet) 140.dp else 100.dp),
                tint = item.highlightColor
            )
        }

        Spacer(modifier = Modifier.height(if (isTablet) 64.dp else 40.dp))

        // TEXT CONTENT
        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (item.subtitle.isNotEmpty()) {
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (item.description.isNotEmpty()) {
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // HIGHLIGHTS / FEATURES
        if (item.features.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.6f else 0.8f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                item.features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = item.highlightColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = feature,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // TRUST BADGES
        if (item.trustBadges.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item.trustBadges.forEach { badge ->
                    TrustBadgeItem(badge = badge)
                }
            }
        }
    }
}
