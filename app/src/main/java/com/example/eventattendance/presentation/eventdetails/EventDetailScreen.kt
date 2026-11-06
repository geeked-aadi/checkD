package com.example.eventattendance.presentation.eventdetails

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventattendance.domain.calculator.ProgressCalculator
import com.example.eventattendance.presentation.common.EventStatusChip
import com.example.eventattendance.presentation.eventdetails.components.ParticipantListItem
import com.example.eventattendance.presentation.eventdetails.components.PendingParticipantListItem
import com.example.eventattendance.presentation.theme.Gray100
import com.example.eventattendance.presentation.theme.Gray500
import com.example.eventattendance.presentation.theme.MonochromeBlack
import com.example.eventattendance.presentation.theme.MonochromeWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    onBackClick: () -> Unit,
    onScanClick: (String) -> Unit,
    onBulkScanClick: (String) -> Unit,
    onParticipantClick: ((String) -> Unit)? = null,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val eventDetail by viewModel.eventDetail.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val checkedInList by viewModel.filteredAttendanceList.collectAsStateWithLifecycle()
    val pendingList by viewModel.filteredPendingList.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()

    val scannedCount by viewModel.scopedScannedCount.collectAsStateWithLifecycle()
    val totalExpected by viewModel.scopedTotalCount.collectAsStateWithLifecycle()

    BackHandler(onBack = onBackClick)

    val progress = if (totalExpected > 0) (scannedCount.toFloat() / totalExpected.toFloat()).coerceIn(0f, 1f) else 0f
    val percentage = (progress * 100).toInt()

    val eventStatus = ProgressCalculator.eventStatus(eventDetail?.startsFrom, eventDetail?.endsOn)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
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
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Action Button (Scan QR)
                Button(
                    onClick = { onScanClick(viewModel.eventId) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MonochromeBlack,
                        contentColor = MonochromeWhite
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Scan QR",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                }

                // Bulk Scan (+) Button
                IconButton(
                    onClick = { onBulkScanClick(viewModel.eventId) },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MonochromeBlack)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Bulk Scan",
                        tint = MonochromeWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // Event Name & Status Chip Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = eventDetail?.eventName ?: "Event",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    EventStatusChip(status = eventStatus)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Count and Percentage Row
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val statsText = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            append("$scannedCount")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = Gray500
                            )
                        ) {
                            append(" / $totalExpected")
                        }
                    }

                    Text(text = statsText, modifier = Modifier.weight(1f))

                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = Gray500
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Linear Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MonochromeBlack,
                    trackColor = Gray100
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = {
                        Text(
                            text = "Search by name, ID, or team…",
                            color = Gray500,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = Gray500,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Gray100,
                        focusedContainerColor = Gray100,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Gray500
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Checked in / Pending
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MonochromeBlack,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MonochromeBlack
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        text = {
                            Text(
                                text = "Checked in (${checkedInList.size})",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        text = {
                            Text(
                                text = "Pending (${pendingList.size})",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tab Content List
                when (selectedTab) {
                    0 -> {
                        if (checkedInList.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No matching checked-in participants" else "No participants checked in yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gray500
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(
                                    items = checkedInList,
                                    key = { it.id }
                                ) { record ->
                                    val phone = viewModel.getPhoneNumber(record.participantId)
                                    ParticipantListItem(
                                        record = record,
                                        currentRole = currentRole,
                                        phoneNumber = phone,
                                        modifier = if (onParticipantClick != null) {
                                            Modifier.clickable { onParticipantClick(record.participantId) }
                                        } else {
                                            Modifier
                                        }
                                    )
                                    HorizontalDivider(color = Gray100, thickness = 1.dp)
                                }
                            }
                        }
                    }
                    1 -> {
                        if (pendingList.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No matching pending participants" else "All participants have checked in!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gray500
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(
                                    items = pendingList,
                                    key = { it.pId }
                                ) { participant ->
                                    PendingParticipantListItem(
                                        participant = participant,
                                        currentRole = currentRole,
                                        modifier = if (onParticipantClick != null) {
                                            Modifier.clickable { onParticipantClick(participant.pId) }
                                        } else {
                                            Modifier
                                        }
                                    )
                                    HorizontalDivider(color = Gray100, thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
