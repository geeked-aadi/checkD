package com.example.eventattendance.presentation.eventdetails.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventattendance.domain.model.EventStatus
import com.example.eventattendance.presentation.theme.Gray500
import com.example.eventattendance.presentation.theme.MonochromeBlack
import com.example.eventattendance.presentation.theme.MonochromeWhite

@Composable
fun StatusBadge(
    status: EventStatus,
    modifier: Modifier = Modifier
) {
    val isOpen = status == EventStatus.OPEN
    val backgroundColor = if (isOpen) MonochromeBlack else Gray500
    val text = if (isOpen) "Open" else "Closed"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = MonochromeWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
