package com.example.netpulse.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.netpulse.data.SpeedResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultShareBottomSheet(
    result: SpeedResult,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            ShareCard(result = result)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { /* TODO: Implement actual image share */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Share as Image")
            }
        }
    }
}
