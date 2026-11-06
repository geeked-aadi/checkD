package com.example.eventattendance.presentation.eventdetails.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.eventattendance.domain.model.TimeBasedStatus
import com.example.eventattendance.presentation.common.EventStatusChip

@Composable
fun StatusBadge(
    status: TimeBasedStatus,
    modifier: Modifier = Modifier
) {
    EventStatusChip(status = status, modifier = modifier)
}
