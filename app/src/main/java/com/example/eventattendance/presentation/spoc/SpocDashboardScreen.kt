package com.example.eventattendance.presentation.spoc

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
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.presentation.common.OfflineStatusBanner
import com.example.eventattendance.presentation.spoc.tabs.SpocEventsTab
import com.example.eventattendance.presentation.spoc.tabs.SpocProfileTab
import com.example.eventattendance.presentation.spoc.tabs.SpocTeamProgressTab
import com.example.eventattendance.presentation.theme.Gray100
import com.example.eventattendance.presentation.theme.Gray500
import com.example.eventattendance.presentation.theme.MonochromeBlack
import com.example.eventattendance.presentation.theme.MonochromeWhite
import com.example.eventattendance.presentation.theme.StatusRed
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpocDashboardScreen(
    onEventClick: (Int) -> Unit,
    onLogout: () -> Unit,
    onParticipantClick: ((String) -> Unit)? = null,
    viewModel: SpocDashboardViewModel = hiltViewModel()
) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val spocData by viewModel.spocData.collectAsStateWithLifecycle()
    val matrix by viewModel.matrix.collectAsStateWithLifecycle()
    val lastUpdated by viewModel.lastUpdated.collectAsStateWithLifecycle()
    val spocEvents by viewModel.spocEvents.collectAsStateWithLifecycle()
    val spocMemberPIds by viewModel.spocMemberPIds.collectAsStateWithLifecycle()

    val spocId = (currentRole as? UserRole.Spoc)?.spocId ?: ""
    val spocName = spocData?.spoc?.name ?: "SPOC $spocId"

    // Auto-refresh lifecycle: 20 seconds while STARTED (Correction 1)
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
                                    text = spocName,
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
                                        text = "SPOC ID: $spocId",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
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
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Outlined.Groups, contentDescription = "Team Progress") },
                    label = { Text("Team Progress") },
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
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
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
                0 -> SpocEventsTab(
                    events = spocEvents,
                    onEventClick = onEventClick
                )
                1 -> SpocTeamProgressTab(
                    spocId = spocId,
                    spocData = spocData,
                    matrix = matrix,
                    currentRole = currentRole,
                    lastUpdated = lastUpdated,
                    onParticipantClick = onParticipantClick
                )
                2 -> SpocProfileTab(
                    spocData = spocData,
                    spocId = spocId,
                    memberCount = spocMemberPIds.size,
                    onLogout = { viewModel.logout(onLogout) }
                )
            }
        }
    }
}
