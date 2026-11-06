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
import com.example.eventattendance.domain.calculator.ProgressCalculator
import com.example.eventattendance.domain.model.AttendanceMatrix
import com.example.eventattendance.domain.model.OverallResult
import com.example.eventattendance.presentation.common.AttendanceProgressBar
import com.example.eventattendance.presentation.common.EventStatusChip
import com.example.eventattendance.presentation.common.LastUpdatedText
import com.example.eventattendance.presentation.theme.Gray100
import com.example.eventattendance.presentation.theme.Gray500
import com.example.eventattendance.presentation.theme.MonochromeBlack
import com.example.eventattendance.presentation.theme.MonochromeWhite

@Composable
fun AdminOverviewTab(
    matrix: AttendanceMatrix?,
    overallResult: OverallResult,
    onEventClick: (Int) -> Unit,
    lastUpdated: Long?,
    modifier: Modifier = Modifier
) {
    val events = matrix?.events ?: emptyList()
    val totalParticipants = matrix?.totalParticipants ?: 0
    val totalCheckIns = remember(events) { events.sumOf { it.presentPIds.size } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Summary Metric Cards
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MonochromeBlack),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Executive Overview",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = MonochromeWhite
                        )
                        LastUpdatedText(epochMillis = lastUpdated)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Overall Attendance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MonochromeWhite.copy(alpha = 0.7f)
                            )
                            Text(
                                text = overallResult.displayPercent,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = MonochromeWhite
                            )
                        }

                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Check-ins",
                                style = MaterialTheme.typography.bodySmall,
                                color = MonochromeWhite.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$totalCheckIns",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = MonochromeWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$totalParticipants total participants",
                            style = MaterialTheme.typography.bodySmall,
                            color = MonochromeWhite.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${events.size} activities scheduled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MonochromeWhite.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // 2. Events Progress List Section
        item {
            Text(
                text = "Activities Progress",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                color = MonochromeBlack
            )
        }

        if (events.isEmpty()) {
            item {
                Text(text = "No activities registered yet.", style = MaterialTheme.typography.bodySmall, color = Gray500)
            }
        } else {
            items(items = events, key = { it.eventId }) { event ->
                val status = ProgressCalculator.eventStatus(event.startsFrom, event.endsOn)
                val presentCount = event.presentPIds.size

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Gray100),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEventClick(event.eventId) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MonochromeWhite,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.ConfirmationNumber,
                                        contentDescription = null,
                                        tint = MonochromeBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = event.eventName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MonochromeBlack,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            EventStatusChip(status = status)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Gray500,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        AttendanceProgressBar(
                            present = presentCount,
                            total = totalParticipants,
                            showCountText = true
                        )
                    }
                }
            }
        }
    }
}
