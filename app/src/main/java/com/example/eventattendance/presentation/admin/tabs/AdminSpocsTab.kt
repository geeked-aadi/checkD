package com.example.eventattendance.presentation.admin.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventattendance.domain.model.EventAttendance
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.presentation.admin.SpocAnalytics
import com.example.eventattendance.presentation.admin.SpocAnalyticsSort
import com.example.eventattendance.presentation.admin.SpocAnalyticsUiState
import com.example.eventattendance.presentation.common.CallButton
import com.example.eventattendance.presentation.theme.Gray100
import com.example.eventattendance.presentation.theme.Gray200
import com.example.eventattendance.presentation.theme.Gray500
import com.example.eventattendance.presentation.theme.MonochromeBlack
import com.example.eventattendance.presentation.theme.MonochromeWhite

@Composable
fun AdminSpocsTab(
    uiState: SpocAnalyticsUiState,
    events: List<EventAttendance>,
    currentRole: UserRole,
    onSelectEvent: (Int?) -> Unit,
    onSortChange: (SpocAnalyticsSort) -> Unit,
    onSpocClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Summary Strip Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MonochromeBlack),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SPOC Analytics",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MonochromeWhite
                        )
                        Text(
                            text = "${uiState.totalSpocs} total SPOCs assigned",
                            style = MaterialTheme.typography.bodySmall,
                            color = MonochromeWhite.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // 2. Event Filter Chips Row
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
                        selected = uiState.selectedEventId == null,
                        onClick = { onSelectEvent(null) },
                        label = { Text("All events") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MonochromeBlack,
                            selectedLabelColor = MonochromeWhite,
                            containerColor = MonochromeWhite,
                            labelColor = MonochromeBlack
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Gray200,
                            selectedBorderColor = MonochromeBlack,
                            enabled = true,
                            selected = uiState.selectedEventId == null
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    events.forEach { event ->
                        val isSelected = uiState.selectedEventId == event.eventId
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onSelectEvent(if (isSelected) null else event.eventId)
                            },
                            label = { Text(event.eventName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MonochromeBlack,
                                selectedLabelColor = MonochromeWhite,
                                containerColor = MonochromeWhite,
                                labelColor = MonochromeBlack
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Gray200,
                                selectedBorderColor = MonochromeBlack,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Sort Options Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPOCs (${uiState.spocs.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                        color = MonochromeBlack
                    )

                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showSortMenu = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Sort,
                                contentDescription = "Sort",
                                modifier = Modifier.size(16.dp),
                                tint = MonochromeBlack
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sort: ${uiState.sortOption.label}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MonochromeBlack
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SpocAnalyticsSort.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            fontWeight = if (uiState.sortOption == option) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSortChange(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. SPOC Cards
        if (uiState.spocs.isEmpty()) {
            item {
                Text(
                    text = "No SPOCs found.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        } else {
            items(items = uiState.spocs, key = { it.spocId }) { spoc ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Gray100),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSpocClick(spoc.spocId) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = spoc.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MonochromeBlack
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ID: ${spoc.spocId} • ${spoc.teamCount} teams • ${spoc.studentCount} students",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray500
                                )
                            }

                            if (!spoc.phone.isNullOrBlank()) {
                                CallButton(
                                    phoneNumber = spoc.phone,
                                    currentRole = currentRole
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Gray500,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status empty state or Progress Bar
                        if (spoc.teamCount == 0 || spoc.studentCount == 0) {
                            Text(
                                text = "No teams assigned to this SPOC yet",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                                color = Gray500
                            )
                        } else {
                            val pctFloat = spoc.pct ?: 0f
                            val pctDisplay = "${(pctFloat * 100).toInt()}%"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Completion: $pctDisplay",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MonochromeBlack
                                )
                                Text(
                                    text = "${spoc.pending} pending",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray500
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { pctFloat },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MonochromeBlack,
                                trackColor = Gray200
                            )
                        }
                    }
                }
            }
        }
    }
}
