package com.example.eventattendance.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.eventattendance.domain.calculator.ProgressCalculator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.example.eventattendance.domain.model.ThemeMode
import com.example.eventattendance.presentation.common.CheckdHomeTopBar
import com.example.eventattendance.presentation.common.OfflineStatusBanner
import com.example.eventattendance.presentation.home.components.EventCard
import com.example.eventattendance.presentation.theme.CheckdTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit,
    onVerifierClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val eventsWithStats by viewModel.eventsWithStats.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val pendingScansCount by viewModel.pendingScansCount.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val sortedEventsWithStats = remember(eventsWithStats) {
        ProgressCalculator.sortEventsByTime(
            events = eventsWithStats,
            startsFrom = { it.event.startsFrom },
            endsOn = { it.event.endsOn },
            eventId = { it.event.eventId }
        )
    }

    Scaffold(
        topBar = {
            Column {
                CheckdHomeTopBar(
                    onVerifierClick = onVerifierClick,
                    onSettingsClick = onSettingsClick
                )
                OfflineStatusBanner(
                    isOffline = isOffline,
                    pendingScansCount = pendingScansCount
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshEvents(force = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (sortedEventsWithStats.isEmpty() && isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (sortedEventsWithStats.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Activities Available",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Activities configured by event administrators will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        items = sortedEventsWithStats,
                        key = { it.event.id }
                    ) { item ->
                        EventCard(
                            eventWithStats = item,
                            onClick = { onEventClick(item.event.id) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Home Screen - Light")
@Composable
private fun HomeScreenLightPreview() {
    CheckdTheme(themeMode = ThemeMode.LIGHT) {
        HomeScreen(onEventClick = {}, onVerifierClick = {}, onSettingsClick = {})
    }
}

@Preview(name = "Home Screen - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenDarkPreview() {
    CheckdTheme(themeMode = ThemeMode.DARK) {
        HomeScreen(onEventClick = {}, onVerifierClick = {}, onSettingsClick = {})
    }
}

