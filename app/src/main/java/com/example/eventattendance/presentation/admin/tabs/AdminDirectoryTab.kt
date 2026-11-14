package com.example.eventattendance.presentation.admin.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.example.eventattendance.domain.model.AdminDirectoryData
import com.example.eventattendance.domain.model.AttendanceMatrix
import com.example.eventattendance.domain.model.TeamMember
import com.example.eventattendance.domain.model.TeamWithMembers
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.presentation.common.CallButton
import com.example.eventattendance.presentation.theme.CheckdTheme
import com.example.eventattendance.presentation.theme.statusColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDirectoryTab(
    directoryData: AdminDirectoryData?,
    matrix: AttendanceMatrix?,
    currentRole: UserRole,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedSpocFilter: String?,
    onSpocFilterChange: (String?) -> Unit,
    selectedTeamFilter: String?,
    onTeamFilterChange: (String?) -> Unit,
    onParticipantClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTeams = directoryData?.teams ?: emptyList()
    val allSpocs = directoryData?.spocs ?: emptyList()
    val allEvents = matrix?.events ?: emptyList()

    // Filter teams based on selected SPOC and Team filter, plus search query
    val filteredTeams = remember(allTeams, searchQuery, selectedSpocFilter, selectedTeamFilter) {
        allTeams.filter { team ->
            // SPOC filter
            if (selectedSpocFilter != null && team.spocId != selectedSpocFilter) return@filter false
            // Team filter
            if (selectedTeamFilter != null && team.teamId != selectedTeamFilter) return@filter false

            if (searchQuery.isBlank()) return@filter true

            val q = searchQuery.trim().lowercase()
            val spocName = allSpocs.find { it.spoc.spocId == team.spocId }?.spoc?.name?.lowercase() ?: ""

            team.teamName.lowercase().contains(q) ||
                    team.teamId.lowercase().contains(q) ||
                    spocName.contains(q) ||
                    team.members.any { m ->
                        m.name.lowercase().contains(q) || m.pId.lowercase().contains(q)
                    }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search & Filters Header
        item {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search by participant, team, or SPOC…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // SPOC filter chips row
                Text(
                    text = "Filter by SPOC:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSpocFilter == null,
                        onClick = { onSpocFilterChange(null) },
                        label = { Text("All SPOCs") },
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
                            selected = selectedSpocFilter == null
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    allSpocs.forEach { spocWithTeams ->
                        val spoc = spocWithTeams.spoc
                        val isSelected = selectedSpocFilter == spoc.spocId
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSpocFilterChange(if (isSelected) null else spoc.spocId) },
                            label = { Text(spoc.name) },
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
            }
        }

        // Teams with participants
        if (filteredTeams.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No participants match '$searchQuery'" else "No teams found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(items = filteredTeams, key = { it.teamId }) { team ->
                val spocName = allSpocs.find { it.spoc.spocId == team.spocId }?.spoc?.name ?: team.spocId
                AdminTeamDirectoryCard(
                    team = team,
                    spocName = spocName,
                    allEvents = allEvents,
                    searchQuery = searchQuery,
                    currentRole = currentRole,
                    onParticipantClick = onParticipantClick
                )
            }
        }
    }
}

@Composable
fun AdminTeamDirectoryCard(
    team: TeamWithMembers,
    spocName: String?,
    allEvents: List<com.example.eventattendance.domain.model.EventAttendance>,
    searchQuery: String,
    currentRole: UserRole,
    onParticipantClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Team Header: Team Name + ID + SPOC Name
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

                    if (!spocName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "SPOC: $spocName",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${team.members.size} members",
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

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(10.dp))

            // Member Rows
            val q = searchQuery.trim().lowercase()
            val displayedMembers = if (q.isNotBlank()) {
                team.members.filter {
                    it.name.lowercase().contains(q) ||
                            it.pId.lowercase().contains(q) ||
                            team.teamName.lowercase().contains(q)
                }.ifEmpty { team.members }
            } else {
                team.members
            }

            if (displayedMembers.isEmpty()) {
                Text(
                    text = "No participants in this team.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                displayedMembers.forEach { member ->
                    AdminParticipantRow(
                        member = member,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminParticipantRow(
    member: TeamMember,
    allEvents: List<com.example.eventattendance.domain.model.EventAttendance>,
    currentRole: UserRole,
    onParticipantClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Name & ID Column
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
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "LEADER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Actions: Call button & Journey button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CallButton(
                        phoneNumber = member.phoneNumber,
                        currentRole = currentRole,
                        size = 30.dp,
                        iconSize = 14.dp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.clickable { onParticipantClick(member.pId) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Journey",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View Journey",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // Expandable per-event attendance strip
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Attendance Summary",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (allEvents.isEmpty()) {
                        Text(
                            text = "No activities registered.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allEvents.forEach { event ->
                                val isPresent = event.presentPIds.contains(member.pId)
                                val shortName = if (event.eventName.length > 12) event.eventName.take(11) + "…" else event.eventName
                                val badgeBg = if (isPresent) MaterialTheme.statusColors.successContainer else MaterialTheme.colorScheme.surface
                                val badgeText = if (isPresent) MaterialTheme.statusColors.onSuccessContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                val badgeBorder = if (isPresent) MaterialTheme.statusColors.success.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = badgeBg,
                                    border = BorderStroke(1.dp, badgeBorder)
                                ) {
                                    Text(
                                        text = "$shortName: ${if (isPresent) "✓" else "–"}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = badgeText,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun AdminDirectoryTabLightPreview() {
    CheckdTheme(darkTheme = false) {
        AdminDirectoryTab(
            directoryData = null,
            matrix = null,
            currentRole = UserRole.Admin,
            searchQuery = "",
            onSearchChange = {},
            selectedSpocFilter = null,
            onSpocFilterChange = {},
            selectedTeamFilter = null,
            onTeamFilterChange = {},
            onParticipantClick = {}
        )
    }
}

@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AdminDirectoryTabDarkPreview() {
    CheckdTheme(darkTheme = true) {
        AdminDirectoryTab(
            directoryData = null,
            matrix = null,
            currentRole = UserRole.Admin,
            searchQuery = "",
            onSearchChange = {},
            selectedSpocFilter = null,
            onSpocFilterChange = {},
            selectedTeamFilter = null,
            onTeamFilterChange = {},
            onParticipantClick = {}
        )
    }
}
