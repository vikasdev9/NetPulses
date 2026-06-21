package com.example.netpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.data.analytics.formatDuration
import com.example.netpulse.ui.theme.CardBorder
import com.example.netpulse.ui.theme.CardSurface
import com.example.netpulse.ui.theme.TextPrimary
import com.example.netpulse.ui.theme.TextSecondary

@Composable
fun ScreenTimeSummaryCard(
    totalTimeMs: Long,
    appCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardSurface)
            .border(0.5.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatDuration(totalTimeMs),
            color = TextPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Across $appCount apps",
            color = TextSecondary,
            fontSize = 13.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Yesterday", color = TextSecondary, fontSize = 11.sp)
                Text(text = "3h 12m", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            
            Box(modifier = Modifier.width(1.dp).height(32.dp).background(CardBorder))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Weekly Avg", color = TextSecondary, fontSize = 11.sp)
                Text(text = "4h 05m", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
