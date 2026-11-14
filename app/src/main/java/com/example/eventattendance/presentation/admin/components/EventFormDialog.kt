package com.example.eventattendance.presentation.admin.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.example.eventattendance.domain.calculator.ProgressCalculator
import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.presentation.common.EventStatusChip
import com.example.eventattendance.presentation.theme.CheckdTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormDialog(
    initialEvent: Event? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, startsFrom: String, endsOn: String) -> Unit
) {
    val isEditMode = initialEvent != null

    var name by remember(initialEvent) { mutableStateOf(initialEvent?.name ?: "") }

    val initialStartLocal = remember(initialEvent) { ProgressCalculator.parseToIstLocalDateTime(initialEvent?.startsFrom) }
    val initialEndLocal = remember(initialEvent) { ProgressCalculator.parseToIstLocalDateTime(initialEvent?.endsOn) }

    var startDate by remember(initialEvent) { mutableStateOf(initialStartLocal?.toLocalDate() ?: LocalDate.now(ProgressCalculator.IST_ZONE)) }
    var startTime by remember(initialEvent) { mutableStateOf(initialStartLocal?.toLocalTime() ?: LocalTime.of(9, 0)) }

    var endDate by remember(initialEvent) { mutableStateOf(initialEndLocal?.toLocalDate() ?: LocalDate.now(ProgressCalculator.IST_ZONE)) }
    var endTime by remember(initialEvent) { mutableStateOf(initialEndLocal?.toLocalTime() ?: LocalTime.of(18, 0)) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    val displayDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val displayTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    val computedStartsFrom = remember(startDate, startTime) {
        startDate.atTime(startTime).format(isoFormatter)
    }
    val computedEndsOn = remember(endDate, endTime) {
        endDate.atTime(endTime).format(isoFormatter)
    }

    val previewStatus = remember(computedStartsFrom, computedEndsOn) {
        ProgressCalculator.eventStatus(computedStartsFrom, computedEndsOn)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isEditMode) "Edit Activity Timeline" else "New Event",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                EventStatusChip(status = previewStatus)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Event Name *") },
                    placeholder = { Text("e.g. Hackathon Keynote, Lunch") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Starts From
                Text(
                    text = "Starts From",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = startDate.format(displayDateFormatter),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Date") },
                            leadingIcon = {
                                Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showStartDatePicker = true }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = startTime.format(displayTimeFormatter),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Time") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showStartTimePicker = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ends On
                Text(
                    text = "Ends On",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = endDate.format(displayDateFormatter),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Date") },
                            leadingIcon = {
                                Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showEndDatePicker = true }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = endTime.format(displayTimeFormatter),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Time") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showEndTimePicker = true }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isEditMode || name.isNotBlank()) {
                        onConfirm(name.trim(), computedStartsFrom, computedEndsOn)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = isEditMode || name.isNotBlank()
            ) {
                Text(if (isEditMode) "Save Time Window" else "Create Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Start Time Picker AlertDialog (Correction 9)
    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = startTime.hour,
            initialMinute = startTime.minute
        )
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showStartTimePicker = false
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        endDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End Time Picker AlertDialog (Correction 9)
    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = endTime.hour,
            initialMinute = endTime.minute
        )
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showEndTimePicker = false
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun EventFormDialogLightPreview() {
    CheckdTheme(darkTheme = false) {
        EventFormDialog(
            initialEvent = Event(
                id = "1",
                name = "Opening Ceremony",
                startsFrom = "2026-11-08T09:00:00",
                endsOn = "2026-11-08T11:00:00"
            ),
            onDismiss = {},
            onConfirm = { _, _, _ -> }
        )
    }
}

@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun EventFormDialogDarkPreview() {
    CheckdTheme(darkTheme = true) {
        EventFormDialog(
            initialEvent = Event(
                id = "1",
                name = "Opening Ceremony",
                startsFrom = "2026-11-08T09:00:00",
                endsOn = "2026-11-08T11:00:00"
            ),
            onDismiss = {},
            onConfirm = { _, _, _ -> }
        )
    }
}
