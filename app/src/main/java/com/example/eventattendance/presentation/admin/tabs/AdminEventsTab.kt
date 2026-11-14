package com.example.eventattendance.presentation.admin.tabs

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.example.eventattendance.domain.calculator.ProgressCalculator
import com.example.eventattendance.domain.model.AttendanceMatrix
import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.domain.model.EventStatus
import com.example.eventattendance.presentation.admin.components.EventFormDialog
import com.example.eventattendance.presentation.common.AttendanceProgressBar
import com.example.eventattendance.presentation.common.EventStatusChip
import com.example.eventattendance.presentation.theme.CheckdTheme
import com.example.eventattendance.util.DateUtils

@Composable
fun AdminEventsTab(
    matrix: AttendanceMatrix?,
    onEventClick: (Int) -> Unit,
    onCreateEvent: (name: String, startsFrom: String, endsOn: String) -> Unit,
    onUpdateTimeWindow: (eventId: Int, startsFrom: String, endsOn: String, name: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<Event?>(null) }

    val events = remember(matrix?.events) {
        val raw = matrix?.events ?: emptyList()
        ProgressCalculator.sortEventsByTime(
            events = raw,
            startsFrom = { it.startsFrom },
            endsOn = { it.endsOn },
            eventId = { it.eventId }
        )
    }
    val totalParticipants = matrix?.totalParticipants ?: 0

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Event")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No Activities Created",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap the + button to create a new activity with custom time window.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(items = events, key = { it.eventId }) { event ->
                    val status = ProgressCalculator.eventStatus(event.startsFrom, event.endsOn)
                    val timeWindow = DateUtils.formatTimeWindow(event.startsFrom, event.endsOn)
                    val presentCount = event.presentPIds.size

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEventClick(event.eventId) }
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = event.eventName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        EventStatusChip(status = status)
                                    }

                                    if (timeWindow.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = timeWindow,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            AttendanceProgressBar(
                                present = presentCount,
                                total = totalParticipants,
                                showCountText = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action: Edit Time Window
                            OutlinedButton(
                                onClick = {
                                    eventToEdit = Event(
                                        id = event.eventId.toString(),
                                        eventId = event.eventId,
                                        name = event.eventName,
                                        startsFrom = event.startsFrom,
                                        endsOn = event.endsOn,
                                        status = EventStatus.OPEN
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.EditCalendar,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Edit Time Window",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Event Dialog
    if (showCreateDialog) {
        EventFormDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, startsFrom, endsOn ->
                onCreateEvent(name, startsFrom, endsOn)
                showCreateDialog = false
            }
        )
    }

    // Edit Time Window Dialog
    if (eventToEdit != null) {
        val editing = eventToEdit!!
        EventFormDialog(
            initialEvent = editing,
            onDismiss = { eventToEdit = null },
            onConfirm = { updatedName, startsFrom, endsOn ->
                onUpdateTimeWindow(editing.eventId, startsFrom, endsOn, updatedName)
                eventToEdit = null
            }
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun AdminEventsTabLightPreview() {
    CheckdTheme(darkTheme = false) {
        AdminEventsTab(
            matrix = null,
            onEventClick = {},
            onCreateEvent = { _, _, _ -> },
            onUpdateTimeWindow = { _, _, _, _ -> }
        )
    }
}

@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AdminEventsTabDarkPreview() {
    CheckdTheme(darkTheme = true) {
        AdminEventsTab(
            matrix = null,
            onEventClick = {},
            onCreateEvent = { _, _, _ -> },
            onUpdateTimeWindow = { _, _, _, _ -> }
        )
    }
}
