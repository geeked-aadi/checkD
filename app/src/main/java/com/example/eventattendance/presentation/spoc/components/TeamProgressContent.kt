package com.example.eventattendance.presentation.spoc.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventattendance.domain.calculator.ProgressCalculator
import com.example.eventattendance.domain.model.AttendanceMatrix
import com.example.eventattendance.domain.model.EventAttendance
import com.example.eventattendance.domain.model.TeamMember
import com.example.eventattendance.domain.model.TeamWithMembers
import com.example.eventattendance.domain.model.UserRole
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.example.eventattendance.presentation.common.AttendanceProgressBar
import com.example.eventattendance.presentation.common.CallButton
import com.example.eventattendance.presentation.common.LastUpdatedText
import com.example.eventattendance.presentation.theme.CheckdTheme
import com.example.eventattendance.presentation.theme.statusColors

enum class TeamSortOption {
    LOWEST_PROGRESS,
    NAME
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamProgressContent(
    spocId: String,
    teams: List<TeamWithMembers>,
    matrix: AttendanceMatrix?,
    currentRole: UserRole,
    lastUpdated: Long?,
    modifier: Modifier = Modifier,
    spocPhone: String? = null,
    hoistedSelectedEventId: Int? = null,
    onSelectEventId: ((Int?) -> Unit)? = null,
    hoistedShowPendingOnly: Boolean? = null,
    onTogglePendingOnly: ((Boolean) -> Unit)? = null,
    hoistedSortOption: TeamSortOption? = null,
    onSortOptionChange: ((TeamSortOption) -> Unit)? = null,
    onParticipantClick: ((String) -> Unit)? = null
) {
    var internalSelectedEventId by remember { mutableStateOf<Int?>(null) }
    var internalShowPendingOnly by remember { mutableStateOf(false) }
    var internalSortOption by remember { mutableStateOf(TeamSortOption.LOWEST_PROGRESS) }

    val selectedEventId = if (onSelectEventId != null) hoistedSelectedEventId else internalSelectedEventId
    val setSelectedEventId: (Int?) -> Unit = { id ->
        if (onSelectEventId != null) onSelectEventId(id) else internalSelectedEventId = id
    }

    val showPendingOnly = if (onTogglePendingOnly != null) hoistedShowPendingOnly ?: false else internalShowPendingOnly
    val setShowPendingOnly: (Boolean) -> Unit = { value ->
        if (onTogglePendingOnly != null) onTogglePendingOnly(value) else internalShowPendingOnly = value
    }

    val sortOption = if (onSortOptionChange != null) hoistedSortOption ?: TeamSortOption.LOWEST_PROGRESS else internalSortOption
    val setSortOption: (TeamSortOption) -> Unit = { opt ->
        if (onSortOptionChange != null) onSortOptionChange(opt) else internalSortOption = opt
    }

    val allEvents = matrix?.events ?: emptyList()
    val allMemberPIds = remember(teams) {
        teams.flatMap { t -> t.members.map { it.pId } }.toSet()
    }

    val overallSpocResult = remember(allMemberPIds, allEvents) {
        ProgressCalculator.overallProgress(allMemberPIds, allEvents)
    }

    // Sort teams
    val sortedTeams = remember(teams, matrix, selectedEventId, sortOption) {
        when (sortOption) {
            TeamSortOption.NAME -> teams.sortedBy { it.teamName.lowercase() }
            TeamSortOption.LOWEST_PROGRESS -> {
                teams.sortedBy { team ->
                    val teamPIds = team.members.map { it.pId }.toSet()
                    if (selectedEventId != null) {
                        val event = allEvents.find { it.eventId == selectedEventId }
                        val present = event?.let { e -> teamPIds.count { it in e.presentPIds } } ?: 0
                        val total = teamPIds.size
                        if (total > 0) present.toFloat() / total.toFloat() else 1f
                    } else {
                        val overall = ProgressCalculator.overallProgress(teamPIds, allEvents)
                        overall.percent ?: 1f
                    }
                }
            }
        }
    }

    // Filter teams when showPendingOnly is enabled
    val displayedTeams = remember(sortedTeams, showPendingOnly, selectedEventId, allEvents) {
        if (!showPendingOnly) {
            sortedTeams
        } else {
            val startedEvents = allEvents.filter { ProgressCalculator.isStarted(it.startsFrom) }
            val targetEvents = if (startedEvents.isNotEmpty()) startedEvents else allEvents
            val selectedEvent = allEvents.find { it.eventId == selectedEventId }

            sortedTeams.filter { team ->
                if (team.members.isEmpty()) {
                    false
                } else if (selectedEventId != null) {
                    if (selectedEvent != null) {
                        team.members.any { it.pId !in selectedEvent.presentPIds }
                    } else {
                        false
                    }
                } else {
                    if (targetEvents.isNotEmpty()) {
                        team.members.any { member ->
                            targetEvents.any { event -> member.pId !in event.presentPIds }
                        }
                    } else {
                        false
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Summary Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SPOC Team Progress",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        LastUpdatedText(epochMillis = lastUpdated)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Overall Progress",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = overallSpocResult.displayPercent,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${teams.size} teams",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${allMemberPIds.size} participants",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!spocPhone.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Contact: $spocPhone",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            CallButton(
                                phoneNumber = spocPhone,
                                currentRole = currentRole,
                                size = 32.dp,
                                iconSize = 16.dp
                            )
                        }
                    }
                }
            }
        }

        // 2. Event Filter Chips Row + Controls
        item {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedEventId == null,
                        onClick = {
                            setSelectedEventId(null)
                        },
                        label = { Text("All events") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = MaterialTheme.colorScheme.outline,
                            selectedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            enabled = true,
                            selected = selectedEventId == null
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    allEvents.forEach { event ->
                        val isSelected = selectedEventId == event.eventId
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val nextId = if (isSelected) null else event.eventId
                                setSelectedEventId(nextId)
                            },
                            label = { Text(event.eventName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sort and Pending Toggle Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sort option toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val nextSort = if (sortOption == TeamSortOption.LOWEST_PROGRESS) {
                                    TeamSortOption.NAME
                                } else {
                                    TeamSortOption.LOWEST_PROGRESS
                                }
                                setSortOption(nextSort)
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Sort,
                            contentDescription = "Sort",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (sortOption == TeamSortOption.LOWEST_PROGRESS) "Lowest progress" else "Name (A-Z)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Show pending only switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "Pending only",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = showPendingOnly,
                            onCheckedChange = { setShowPendingOnly(it) },
                            enabled = true,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.size(32.dp, 20.dp)
                        )
                    }
                }
            }
        }

        // 3. Teams List
        if (displayedTeams.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showPendingOnly) {
                            if (selectedEventId != null) "All participants checked in for this event!"
                            else "All participants checked in for all activities!"
                        } else {
                            "No teams assigned."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(
                items = displayedTeams,
                key = { it.teamId }
            ) { team ->
                TeamProgressCard(
                    team = team,
                    selectedEventId = selectedEventId,
                    showPendingOnly = showPendingOnly,
                    allEvents = allEvents,
                    currentRole = currentRole,
                    onParticipantClick = onParticipantClick
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamProgressCard(
    team: TeamWithMembers,
    selectedEventId: Int?,
    showPendingOnly: Boolean,
    allEvents: List<EventAttendance>,
    currentRole: UserRole,
    onParticipantClick: ((String) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "expand_rotation"
    )

    val teamPIds = remember(team) { team.members.map { it.pId }.toSet() }

    // Leader contact lookup
    val leaderMember = team.members.find { it.isLeader || it.pId == team.teamLeaderId }
    val leaderPhone = leaderMember?.phoneNumber

    // Overall progress for this team
    val teamOverall = remember(teamPIds, allEvents) {
        ProgressCalculator.overallProgress(teamPIds, allEvents)
    }

    // Selected event progress for this team
    val selectedEvent = remember(allEvents, selectedEventId) {
        allEvents.find { it.eventId == selectedEventId }
    }
    val selectedEventProgress = remember(teamPIds, selectedEvent) {
        selectedEvent?.let { ProgressCalculator.eventProgress(teamPIds, it.presentPIds) }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Team Name & ID + Leader + CallButton
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
                            text = team.teamName,
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
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = team.teamId,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val leaderDisplay = team.teamLeaderName ?: team.teamLeaderId ?: "Not assigned"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Leader: $leaderDisplay",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!leaderPhone.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            CallButton(
                                phoneNumber = leaderPhone,
                                currentRole = currentRole,
                                size = 26.dp,
                                iconSize = 13.dp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${team.members.size} members",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Display
            if (selectedEventId != null && selectedEventProgress != null) {
                // Single event mode: show that event's x/y and bar
                AttendanceProgressBar(
                    present = selectedEventProgress.present,
                    total = selectedEventProgress.total,
                    showCountText = true
                )
            } else {
                // All events mode: overall progress bar and percent
                val presentSum = teamOverall.presentSum
                val expectedSum = teamOverall.expectedSum
                AttendanceProgressBar(
                    present = presentSum,
                    total = expectedSum,
                    showCountText = true
                )

                // Per-event chips row (e.g. "Keynote 8/10")
                if (allEvents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allEvents.forEach { event ->
                            val progress = ProgressCalculator.eventProgress(teamPIds, event.presentPIds)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${event.eventName}: ${progress.displayFraction}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Expanded view: Member rows
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter members if "Show pending only" is active
                    val displayedMembers = if (showPendingOnly) {
                        if (selectedEvent != null) {
                            team.members.filter { it.pId !in selectedEvent.presentPIds }
                        } else if (selectedEventId != null) {
                            emptyList()
                        } else {
                            val startedEvents = allEvents.filter { ProgressCalculator.isStarted(it.startsFrom) }
                            val targetEvents = if (startedEvents.isNotEmpty()) startedEvents else allEvents
                            team.members.filter { member ->
                                targetEvents.any { event -> member.pId !in event.presentPIds }
                            }
                        }
                    } else {
                        team.members
                    }

                    if (displayedMembers.isEmpty()) {
                        Text(
                            text = if (showPendingOnly) {
                                if (selectedEventId != null) "All members checked in for this event!"
                                else "All members checked in for all activities!"
                            } else {
                                "No members listed."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        displayedMembers.forEach { member ->
                            MemberProgressRow(
                                member = member,
                                selectedEventId = selectedEventId,
                                allEvents = allEvents,
                                currentRole = currentRole,
                                onParticipantClick = onParticipantClick
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemberProgressRow(
    member: TeamMember,
    selectedEventId: Int?,
    allEvents: List<EventAttendance>,
    currentRole: UserRole,
    onParticipantClick: ((String) -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable(enabled = onParticipantClick != null) {
                onParticipantClick?.invoke(member.pId)
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (member.isLeader) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LEADER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "ID: ${member.pId}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Per-event attendance strip
            Spacer(modifier = Modifier.height(6.dp))
            if (selectedEventId != null) {
                val event = allEvents.find { it.eventId == selectedEventId }
                val isPresent = event?.presentPIds?.contains(member.pId) == true
                val badgeBg = if (isPresent) MaterialTheme.statusColors.successContainer else MaterialTheme.colorScheme.surface
                val badgeText = if (isPresent) MaterialTheme.statusColors.onSuccessContainer else MaterialTheme.colorScheme.onSurfaceVariant
                val badgeBorder = if (isPresent) MaterialTheme.statusColors.success else MaterialTheme.colorScheme.outline

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeBg)
                            .border(1.dp, badgeBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isPresent) "✓ Present" else "– Pending",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = badgeText
                        )
                    }
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allEvents.forEach { event ->
                        val isPresent = event.presentPIds.contains(member.pId)
                        val shortName = if (event.eventName.length > 8) event.eventName.take(7) + "…" else event.eventName
                        val badgeBg = if (isPresent) MaterialTheme.statusColors.successContainer else MaterialTheme.colorScheme.surface
                        val badgeText = if (isPresent) MaterialTheme.statusColors.onSuccessContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        val badgeBorder = if (isPresent) MaterialTheme.statusColors.success else MaterialTheme.colorScheme.outline

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeBg)
                                .border(1.dp, badgeBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$shortName: ${if (isPresent) "✓" else "–"}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                ),
                                color = badgeText
                            )
                        }
                    }
                }
            }
        }

        // CallButton (role-gated: only Spoc/Admin see this)
        CallButton(
            phoneNumber = member.phoneNumber,
            currentRole = currentRole
        )

        if (onParticipantClick != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View Journey",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun TeamProgressContentLightPreview() {
    CheckdTheme(darkTheme = false) {
        TeamProgressContent(
            spocId = "SPOC-01",
            teams = listOf(
                TeamWithMembers(
                    teamId = "T-01",
                    teamName = "Alpha Squad",
                    spocId = "SPOC-01",
                    teamLeaderId = "P-101",
                    teamLeaderName = "Alice",
                    members = listOf(
                        TeamMember(
                            pId = "P-101",
                            name = "Alice",
                            isLeader = true,
                            phoneNumber = "1234567890"
                        ),
                        TeamMember(
                            pId = "P-102",
                            name = "Bob",
                            isLeader = false,
                            phoneNumber = "1234567891"
                        )
                    )
                )
            ),
            matrix = null,
            currentRole = UserRole.Spoc("SPOC-01"),
            lastUpdated = System.currentTimeMillis()
        )
    }
}

@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun TeamProgressContentDarkPreview() {
    CheckdTheme(darkTheme = true) {
        TeamProgressContent(
            spocId = "SPOC-01",
            teams = listOf(
                TeamWithMembers(
                    teamId = "T-01",
                    teamName = "Alpha Squad",
                    spocId = "SPOC-01",
                    teamLeaderId = "P-101",
                    teamLeaderName = "Alice",
                    members = listOf(
                        TeamMember(
                            pId = "P-101",
                            name = "Alice",
                            isLeader = true,
                            phoneNumber = "1234567890"
                        ),
                        TeamMember(
                            pId = "P-102",
                            name = "Bob",
                            isLeader = false,
                            phoneNumber = "1234567891"
                        )
                    )
                )
            ),
            matrix = null,
            currentRole = UserRole.Spoc("SPOC-01"),
            lastUpdated = System.currentTimeMillis()
        )
    }
}
