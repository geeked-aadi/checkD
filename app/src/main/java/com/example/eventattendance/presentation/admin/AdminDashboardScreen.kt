package com.example.eventattendance.presentation.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.eventattendance.presentation.admin.tabs.AdminDirectoryTab
import com.example.eventattendance.presentation.admin.tabs.AdminEventsTab
import com.example.eventattendance.presentation.admin.tabs.AdminOverviewTab
import com.example.eventattendance.presentation.admin.tabs.AdminSettingsTab
import com.example.eventattendance.presentation.admin.tabs.AdminSpocsTab
import com.example.eventattendance.presentation.common.OfflineStatusBanner
import com.example.eventattendance.presentation.theme.Gray100
import com.example.eventattendance.presentation.theme.Gray500
import com.example.eventattendance.presentation.theme.MonochromeBlack
import com.example.eventattendance.presentation.theme.StatusRed
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onEventClick: (Int) -> Unit,
    onSpocClick: (String) -> Unit,
    onParticipantClick: (String) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: AdminViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val matrix by viewModel.matrix.collectAsStateWithLifecycle()
    val lastUpdated by viewModel.lastUpdated.collectAsStateWithLifecycle()
    val directoryData by viewModel.directoryData.collectAsStateWithLifecycle()
    val overallResult by viewModel.overallResult.collectAsStateWithLifecycle()
    val spocAnalyticsUiState by viewModel.spocAnalyticsUiState.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()

    val directorySearch by viewModel.directorySearch.collectAsStateWithLifecycle()
    val filterSpocId by viewModel.filterSpocId.collectAsStateWithLifecycle()
    val filterTeamId by viewModel.filterTeamId.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        if (selectedTab != 0) {
            viewModel.selectTab(0)
        } else {
            onBack()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.actionMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Auto-refresh lifecycle: 20s while STARTED (Correction 1)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                viewModel.refresh()
                delay(20_000)
            }
        }
    }

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(
                                    text = "Administrator",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    ),
                                    color = MonochromeBlack
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Gray100)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SUPERADMIN CONSOLE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        ),
                                        color = Gray500
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = MonochromeBlack
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Logout,
                                            contentDescription = null,
                                            tint = StatusRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sign Out", color = StatusRed, fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.logout(onLogout)
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                OfflineStatusBanner(
                    isOffline = isOffline,
                    pendingScansCount = 0
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Outlined.Dashboard, contentDescription = "Overview") },
                    label = { Text("Overview") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MonochromeBlack,
                        selectedTextColor = MonochromeBlack,
                        indicatorColor = Gray100,
                        unselectedIconColor = Gray500,
                        unselectedTextColor = Gray500
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Outlined.Event, contentDescription = "Events") },
                    label = { Text("Events") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MonochromeBlack,
                        selectedTextColor = MonochromeBlack,
                        indicatorColor = Gray100,
                        unselectedIconColor = Gray500,
                        unselectedTextColor = Gray500
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Outlined.Badge, contentDescription = "Directory") },
                    label = { Text("Directory") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MonochromeBlack,
                        selectedTextColor = MonochromeBlack,
                        indicatorColor = Gray100,
                        unselectedIconColor = Gray500,
                        unselectedTextColor = Gray500
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.Outlined.Analytics, contentDescription = "SPOCs") },
                    label = { Text("SPOCs") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MonochromeBlack,
                        selectedTextColor = MonochromeBlack,
                        indicatorColor = Gray100,
                        unselectedIconColor = Gray500,
                        unselectedTextColor = Gray500
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MonochromeBlack,
                        selectedTextColor = MonochromeBlack,
                        indicatorColor = Gray100,
                        unselectedIconColor = Gray500,
                        unselectedTextColor = Gray500
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> AdminOverviewTab(
                    matrix = matrix,
                    overallResult = overallResult,
                    onEventClick = onEventClick,
                    lastUpdated = lastUpdated
                )
                1 -> AdminEventsTab(
                    matrix = matrix,
                    onEventClick = onEventClick,
                    onCreateEvent = viewModel::createEvent,
                    onUpdateTimeWindow = viewModel::updateEventTimeWindow
                )
                2 -> AdminDirectoryTab(
                    directoryData = directoryData,
                    matrix = matrix,
                    currentRole = currentRole,
                    searchQuery = directorySearch,
                    onSearchChange = viewModel::setDirectorySearch,
                    selectedSpocFilter = filterSpocId,
                    onSpocFilterChange = viewModel::setFilterSpoc,
                    selectedTeamFilter = filterTeamId,
                    onTeamFilterChange = viewModel::setFilterTeam,
                    onParticipantClick = onParticipantClick
                )
                3 -> AdminSpocsTab(
                    uiState = spocAnalyticsUiState,
                    events = matrix?.events ?: emptyList(),
                    currentRole = currentRole,
                    onSelectEvent = viewModel::setSelectedSpocEvent,
                    onSortChange = viewModel::setSpocAnalyticsSort,
                    onSpocClick = onSpocClick
                )
                4 -> AdminSettingsTab(
                    isRefreshing = isRefreshing,
                    onRefreshParticipants = viewModel::refresh,
                    onLogout = { viewModel.logout(onLogout) }
                )
            }
        }
    }
}
