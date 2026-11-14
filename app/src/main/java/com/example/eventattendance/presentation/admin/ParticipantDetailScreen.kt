package com.example.eventattendance.presentation.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.border
import androidx.compose.ui.tooling.preview.Preview
import com.example.eventattendance.domain.model.TimeBasedStatus
import com.example.eventattendance.presentation.common.CallButton
import com.example.eventattendance.presentation.common.EventStatusChip
import com.example.eventattendance.presentation.theme.CheckdTheme
import com.example.eventattendance.presentation.theme.statusColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ParticipantDetailViewModel = hiltViewModel()
) {
    val journey by viewModel.participantJourney.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()

    BackHandler(onBack = onBackClick)

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                viewModel.refresh()
                delay(20_000)
            }
        }
    }

    val participant = journey?.participant
    val displayName = participant?.name ?: "Participant ${viewModel.pId}"
    val phone = participant?.phonenumber

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Participant Journey · ID: ${viewModel.pId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!phone.isNullOrBlank()) {
                            CallButton(
                                phoneNumber = phone,
                                currentRole = currentRole,
                                size = 30.dp,
                                iconSize = 15.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (journey == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                val currentJourney = journey!!

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Participant Profile & Overall Attendance Summary Card
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = currentJourney.participant.name ?: currentJourney.participant.pId,
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (currentJourney.isLeader) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(MaterialTheme.colorScheme.primary)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "LEADER",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "ID: ${currentJourney.participant.pId}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    val pct = currentJourney.completionPercent ?: 0f
                                    val pctDisplay = "${(pct * 100).toInt()}%"

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = pctDisplay,
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 26.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${currentJourney.attendedEvents} of ${currentJourney.totalEvents} events",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                LinearProgressIndicator(
                                    progress = { currentJourney.completionPercent ?: 0f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }

                    // 2. Team & SPOC Information Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Affiliation Details",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Team Info Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Groups,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Team",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${currentJourney.participant.teamName ?: "No Team"} (${currentJourney.participant.teamId ?: "N/A"})",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(10.dp))

                                // SPOC Info Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Badge,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Assigned SPOC",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${currentJourney.participant.spocName ?: "Unassigned"} (${currentJourney.participant.spocId ?: "N/A"})",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (!currentJourney.participant.spocPhonenumber.isNullOrBlank()) {
                                        CallButton(
                                            phoneNumber = currentJourney.participant.spocPhonenumber,
                                            currentRole = currentRole,
                                            size = 30.dp,
                                            iconSize = 15.dp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Activity Journey Timeline Header
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Entire Activity Journey",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${currentJourney.attendedEvents}/${currentJourney.totalEvents} Checked In",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 4. Chronological Timeline Items
                    if (currentJourney.timeline.isEmpty()) {
                        item {
                            Text(
                                text = "No activities registered yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = currentJourney.timeline,
                            key = { _, item -> item.eventId }
                        ) { index, journeyItem ->
                            val isLast = index == currentJourney.timeline.lastIndex

                            ParticipantTimelineItemRow(
                                item = journeyItem,
                                isLast = isLast
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantTimelineItemRow(
    item: ParticipantEventJourneyItem,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Timeline Step Column (Marker + Vertical Line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            // Marker circle based on attendance & event status
            val (markerBg, markerIconColor, markerIcon) = when {
                item.isPresent -> Triple(
                    MaterialTheme.statusColors.successContainer,
                    MaterialTheme.statusColors.onSuccessContainer,
                    Icons.Default.Check
                )
                item.timeStatus == TimeBasedStatus.LIVE -> Triple(
                    MaterialTheme.statusColors.warningContainer,
                    MaterialTheme.statusColors.onWarningContainer,
                    Icons.Outlined.Schedule
                )
                item.timeStatus == TimeBasedStatus.ENDED -> Triple(
                    MaterialTheme.statusColors.dangerContainer,
                    MaterialTheme.statusColors.onDangerContainer,
                    Icons.Default.Close
                )
                else -> Triple(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    Icons.Outlined.Schedule
                ) // UPCOMING
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(markerBg)
                    .border(1.dp, markerIconColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = markerIcon,
                    contentDescription = null,
                    tint = markerIconColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            // Connecting vertical line to next item
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Content Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.eventName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    EventStatusChip(status = item.timeStatus)
                }

                if (item.formattedTimeWindow.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.formattedTimeWindow,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Attendance Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val (statusLabel, statusBg, statusColor) = when {
                        item.isPresent -> Triple(
                            "Checked In",
                            MaterialTheme.statusColors.successContainer,
                            MaterialTheme.statusColors.onSuccessContainer
                        )
                        item.timeStatus == TimeBasedStatus.LIVE -> Triple(
                            "Pending Check-in",
                            MaterialTheme.statusColors.warningContainer,
                            MaterialTheme.statusColors.onWarningContainer
                        )
                        item.timeStatus == TimeBasedStatus.ENDED -> Triple(
                            "Absent / Missed",
                            MaterialTheme.statusColors.dangerContainer,
                            MaterialTheme.statusColors.onDangerContainer
                        )
                        else -> Triple(
                            "Upcoming Activity",
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusBg)
                            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = statusColor
                        )
                    }

                    Text(
                        text = if (item.isPresent) "Attendance Verified" else if (item.timeStatus == TimeBasedStatus.LIVE) "Awaiting Scan" else if (item.timeStatus == TimeBasedStatus.ENDED) "No Check-in" else "Not Started",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun ParticipantTimelineItemRowLightPreview() {
    CheckdTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            ParticipantTimelineItemRow(
                item = ParticipantEventJourneyItem(
                    eventId = 1,
                    eventName = "Opening Ceremony",
                    startsFrom = null,
                    endsOn = null,
                    formattedTimeWindow = "09:00 AM - 10:00 AM",
                    timeStatus = TimeBasedStatus.ENDED,
                    isPresent = true
                ),
                isLast = false
            )
            ParticipantTimelineItemRow(
                item = ParticipantEventJourneyItem(
                    eventId = 2,
                    eventName = "Hackathon Phase 1",
                    startsFrom = null,
                    endsOn = null,
                    formattedTimeWindow = "10:00 AM - 01:00 PM",
                    timeStatus = TimeBasedStatus.LIVE,
                    isPresent = false
                ),
                isLast = true
            )
        }
    }
}

@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ParticipantTimelineItemRowDarkPreview() {
    CheckdTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            ParticipantTimelineItemRow(
                item = ParticipantEventJourneyItem(
                    eventId = 1,
                    eventName = "Opening Ceremony",
                    startsFrom = null,
                    endsOn = null,
                    formattedTimeWindow = "09:00 AM - 10:00 AM",
                    timeStatus = TimeBasedStatus.ENDED,
                    isPresent = true
                ),
                isLast = false
            )
            ParticipantTimelineItemRow(
                item = ParticipantEventJourneyItem(
                    eventId = 2,
                    eventName = "Hackathon Phase 1",
                    startsFrom = null,
                    endsOn = null,
                    formattedTimeWindow = "10:00 AM - 01:00 PM",
                    timeStatus = TimeBasedStatus.LIVE,
                    isPresent = false
                ),
                isLast = true
            )
        }
    }
}
