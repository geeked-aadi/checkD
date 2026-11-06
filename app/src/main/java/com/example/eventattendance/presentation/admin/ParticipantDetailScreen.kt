package com.example.eventattendance.presentation.admin

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
import com.example.eventattendance.domain.model.TimeBasedStatus
import com.example.eventattendance.presentation.common.CallButton
import com.example.eventattendance.presentation.common.EventStatusChip
import com.example.eventattendance.presentation.theme.Gray100
import com.example.eventattendance.presentation.theme.Gray200
import com.example.eventattendance.presentation.theme.Gray500
import com.example.eventattendance.presentation.theme.MonochromeBlack
import com.example.eventattendance.presentation.theme.MonochromeWhite
import com.example.eventattendance.presentation.theme.StatusGreen
import com.example.eventattendance.presentation.theme.StatusGreenBg
import com.example.eventattendance.presentation.theme.StatusRed
import com.example.eventattendance.presentation.theme.StatusRedBg
import com.example.eventattendance.presentation.theme.StatusYellow
import com.example.eventattendance.presentation.theme.StatusYellowBg
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
                                color = MonochromeBlack
                            )
                            Text(
                                text = "Participant Journey · ID: ${viewModel.pId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                        }

                        if (!phone.isNullOrBlank()) {
                            CallButton(
                                phoneNumber = phone,
                                currentRole = currentRole,
                                size = 36.dp,
                                iconSize = 18.dp
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
                            tint = MonochromeBlack
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MonochromeBlack
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
                    CircularProgressIndicator(color = MonochromeBlack)
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
                            colors = CardDefaults.cardColors(containerColor = MonochromeBlack),
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
                                                color = MonochromeWhite
                                            )
                                            if (currentJourney.isLeader) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(MonochromeWhite)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "LEADER",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = MonochromeBlack
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "ID: ${currentJourney.participant.pId}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MonochromeWhite.copy(alpha = 0.7f)
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
                                            color = MonochromeWhite
                                        )
                                        Text(
                                            text = "${currentJourney.attendedEvents} of ${currentJourney.totalEvents} events",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MonochromeWhite.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                LinearProgressIndicator(
                                    progress = { currentJourney.completionPercent ?: 0f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MonochromeWhite,
                                    trackColor = MonochromeWhite.copy(alpha = 0.25f)
                                )
                            }
                        }
                    }

                    // 2. Team & SPOC Information Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Gray100),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Affiliation Details",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = MonochromeBlack
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
                                        tint = MonochromeBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Team",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Gray500
                                        )
                                        Text(
                                            text = "${currentJourney.participant.teamName ?: "No Team"} (${currentJourney.participant.teamId ?: "N/A"})",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MonochromeBlack
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Gray200)
                                Spacer(modifier = Modifier.height(10.dp))

                                // SPOC Info Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Badge,
                                        contentDescription = null,
                                        tint = MonochromeBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Assigned SPOC",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Gray500
                                        )
                                        Text(
                                            text = "${currentJourney.participant.spocName ?: "Unassigned"} (${currentJourney.participant.spocId ?: "N/A"})",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MonochromeBlack
                                        )
                                    }

                                    if (!currentJourney.participant.spocPhonenumber.isNullOrBlank()) {
                                        CallButton(
                                            phoneNumber = currentJourney.participant.spocPhonenumber,
                                            currentRole = currentRole,
                                            size = 32.dp,
                                            iconSize = 16.dp
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
                                color = MonochromeBlack,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Gray100)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${currentJourney.attendedEvents}/${currentJourney.totalEvents} Checked In",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = MonochromeBlack
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
                                color = Gray500
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
                item.isPresent -> Triple(StatusGreenBg, StatusGreen, Icons.Default.Check)
                item.timeStatus == TimeBasedStatus.LIVE -> Triple(StatusYellowBg, StatusYellow, Icons.Outlined.Schedule)
                item.timeStatus == TimeBasedStatus.ENDED -> Triple(StatusRedBg, StatusRed, Icons.Default.Close)
                else -> Triple(Gray100, Gray500, Icons.Outlined.Schedule) // UPCOMING
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(markerBg),
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
                        .background(Gray200)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Content Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Gray100),
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
                        color = MonochromeBlack,
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
                        color = Gray500
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
                        item.isPresent -> Triple("Checked In", StatusGreenBg, StatusGreen)
                        item.timeStatus == TimeBasedStatus.LIVE -> Triple("Pending Check-in", StatusYellowBg, StatusYellow)
                        item.timeStatus == TimeBasedStatus.ENDED -> Triple("Absent / Missed", StatusRedBg, StatusRed)
                        else -> Triple("Upcoming Activity", Gray200, MonochromeBlack)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusBg)
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
                        color = Gray500
                    )
                }
            }
        }
    }
}
