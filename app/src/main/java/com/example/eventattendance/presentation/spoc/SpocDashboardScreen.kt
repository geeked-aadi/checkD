package com.example.eventattendance.presentation.spoc

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
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.presentation.common.OfflineStatusBanner
import com.example.eventattendance.presentation.spoc.tabs.SpocEventsTab
import com.example.eventattendance.presentation.spoc.tabs.SpocProfileTab
import com.example.eventattendance.presentation.spoc.tabs.SpocTeamProgressTab
import com.example.eventattendance.presentation.theme.CheckdTheme
import com.example.eventattendance.presentation.theme.statusColors
import kotlinx.coroutines.delay

@Composable
fun SpocDashboardScreen(
    onEventClick: (Int) -> Unit,
    onLogout: () -> Unit,
    onParticipantClick: ((String) -> Unit)? = null,
    onVerifierClick: () -> Unit = {},
    onBack: () -> Unit = {},
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

    BackHandler {
        if (selectedTab != 0) {
            viewModel.selectTab(0)
        } else {
            onBack()
        }
    }

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

    SpocDashboardContent(
        spocId = spocId,
        spocName = spocName,
        isOffline = isOffline,
        selectedTab = selectedTab,
        onSelectTab = viewModel::selectTab,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        onLogout = { viewModel.logout(onLogout) },
        onVerifierClick = onVerifierClick,
        spocEvents = spocEvents,
        onEventClick = onEventClick,
        spocData = spocData,
        matrix = matrix,
        currentRole = currentRole,
        lastUpdated = lastUpdated,
        onParticipantClick = onParticipantClick,
        memberCount = spocMemberPIds.size
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpocDashboardContent(
    spocId: String,
    spocName: String,
    isOffline: Boolean,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    spocEvents: List<SpocEventCardData>,
    onEventClick: (Int) -> Unit,
    spocData: com.example.eventattendance.domain.model.SpocDashboardData?,
    matrix: com.example.eventattendance.domain.model.AttendanceMatrix?,
    currentRole: UserRole,
    lastUpdated: Long?,
    onParticipantClick: ((String) -> Unit)?,
    memberCount: Int,
    onVerifierClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = spocName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
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
                                    text = "SPOC: $spocId",
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
                    },
                    actions = {
                        IconButton(onClick = onVerifierClick) {
                            Icon(
                                imageVector = Icons.Outlined.QrCodeScanner,
                                contentDescription = "QR Verifier",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onBackground
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
                                            tint = MaterialTheme.statusColors.danger,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Sign Out",
                                            color = MaterialTheme.statusColors.danger,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onLogout()
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
            val navItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { onSelectTab(0) },
                    icon = { Icon(Icons.Outlined.Event, contentDescription = "Events") },
                    label = { Text("Events") },
                    colors = navItemColors
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { onSelectTab(1) },
                    icon = { Icon(Icons.Outlined.Groups, contentDescription = "Team Progress") },
                    label = { Text("Team Progress") },
                    colors = navItemColors
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { onSelectTab(2) },
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = navItemColors
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
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
                    memberCount = memberCount,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun SpocDashboardContentLightPreview() {
    CheckdTheme(darkTheme = false) {
        SpocDashboardContent(
            spocId = "SPOC-01",
            spocName = "Jane Doe",
            isOffline = false,
            selectedTab = 0,
            onSelectTab = {},
            isRefreshing = false,
            onRefresh = {},
            onLogout = {},
            spocEvents = emptyList(),
            onEventClick = {},
            spocData = null,
            matrix = null,
            currentRole = UserRole.Spoc("SPOC-01"),
            lastUpdated = System.currentTimeMillis(),
            onParticipantClick = null,
            memberCount = 12
        )
    }
}

@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SpocDashboardContentDarkPreview() {
    CheckdTheme(darkTheme = true) {
        SpocDashboardContent(
            spocId = "SPOC-01",
            spocName = "Jane Doe",
            isOffline = false,
            selectedTab = 0,
            onSelectTab = {},
            isRefreshing = false,
            onRefresh = {},
            onLogout = {},
            spocEvents = emptyList(),
            onEventClick = {},
            spocData = null,
            matrix = null,
            currentRole = UserRole.Spoc("SPOC-01"),
            lastUpdated = System.currentTimeMillis(),
            onParticipantClick = null,
            memberCount = 12
        )
    }
}
