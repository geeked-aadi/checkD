package com.example.eventattendance.presentation.spoc.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.example.eventattendance.domain.calculator.ProgressCalculator
import com.example.eventattendance.domain.model.ProgressResult
import com.example.eventattendance.presentation.common.AttendanceProgressBar
import com.example.eventattendance.presentation.common.EventStatusChip
import com.example.eventattendance.presentation.spoc.SpocEventCardData
import com.example.eventattendance.presentation.theme.CheckdTheme
import com.example.eventattendance.util.DateUtils

@Composable
fun SpocEventsTab(
    events: List<SpocEventCardData>,
    onEventClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedEvents = remember(events) {
        ProgressCalculator.sortEventsByTime(
            events = events,
            startsFrom = { it.startsFrom },
            endsOn = { it.endsOn },
            eventId = { it.eventId }
        )
    }

    if (sortedEvents.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Activities Available",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Events configured by event administrators will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(
                items = sortedEvents,
                key = { it.eventId }
            ) { item ->
                SpocEventCard(
                    event = item,
                    onClick = { onEventClick(item.eventId) }
                )
            }
        }
    }
}

@Composable
fun SpocEventCard(
    event: SpocEventCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = ProgressCalculator.eventStatus(event.startsFrom, event.endsOn)
    val timeWindow = DateUtils.formatTimeWindow(event.startsFrom, event.endsOn)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Icon + Name + Status Chip + Chevron
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.ConfirmationNumber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = event.eventName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        EventStatusChip(status = status)
                    }

                    if (timeWindow.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = timeWindow,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open Event Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar scoped to this SPOC's members only
            AttendanceProgressBar(
                present = event.progress.present,
                total = event.progress.total,
                showCountText = true
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun SpocEventsTabLightPreview() {
    CheckdTheme(darkTheme = false) {
        SpocEventsTab(
            events = listOf(
                SpocEventCardData(
                    eventId = 1,
                    eventName = "Opening Keynote",
                    startsFrom = "2026-11-08T09:00:00",
                    endsOn = "2026-11-08T11:00:00",
                    progress = ProgressResult(present = 18, total = 20, percent = 0.9f)
                )
            ),
            onEventClick = {}
        )
    }
}

@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SpocEventsTabDarkPreview() {
    CheckdTheme(darkTheme = true) {
        SpocEventsTab(
            events = listOf(
                SpocEventCardData(
                    eventId = 1,
                    eventName = "Opening Keynote",
                    startsFrom = "2026-11-08T09:00:00",
                    endsOn = "2026-11-08T11:00:00",
                    progress = ProgressResult(present = 18, total = 20, percent = 0.9f)
                )
            ),
            onEventClick = {}
        )
    }
}
