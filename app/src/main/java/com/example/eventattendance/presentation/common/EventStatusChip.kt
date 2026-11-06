package com.example.eventattendance.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventattendance.domain.model.TimeBasedStatus
import com.example.eventattendance.presentation.theme.Gray100
import com.example.eventattendance.presentation.theme.Gray700
import com.example.eventattendance.presentation.theme.Gray800
import com.example.eventattendance.presentation.theme.MonochromeWhite
import com.example.eventattendance.presentation.theme.StatusGreen
import com.example.eventattendance.presentation.theme.StatusGreenBg

@Composable
fun EventStatusChip(
    status: TimeBasedStatus,
    modifier: Modifier = Modifier
) {
    val (containerColor, textColor) = when (status) {
        TimeBasedStatus.UPCOMING -> Pair(Gray100, Gray700)
        TimeBasedStatus.LIVE -> Pair(StatusGreenBg, StatusGreen)
        TimeBasedStatus.ENDED -> Pair(Gray800, MonochromeWhite)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.3.sp
            ),
            color = textColor
        )
    }
}
