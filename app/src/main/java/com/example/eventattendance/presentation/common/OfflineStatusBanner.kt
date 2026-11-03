package com.example.eventattendance.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventattendance.presentation.theme.Gray800
import com.example.eventattendance.presentation.theme.MonochromeWhite

@Composable
fun OfflineStatusBanner(
    isOffline: Boolean,
    pendingScansCount: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOffline || pendingScansCount > 0,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Gray800)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = if (isOffline) Icons.Filled.CloudOff else Icons.Filled.Sync,
                    contentDescription = null,
                    tint = MonochromeWhite,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                val message = when {
                    isOffline && pendingScansCount > 0 -> "Offline Mode • $pendingScansCount scan(s) queued locally"
                    isOffline -> "Offline Mode • Scans will sync when connected"
                    else -> "Syncing $pendingScansCount scan(s) with cloud…"
                }

                Text(
                    text = message,
                    color = MonochromeWhite,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
