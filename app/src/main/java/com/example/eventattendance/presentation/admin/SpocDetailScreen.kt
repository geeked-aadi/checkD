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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.presentation.common.CallButton
import com.example.eventattendance.presentation.spoc.components.TeamProgressContent
import com.example.eventattendance.presentation.theme.CheckdTheme
import kotlinx.coroutines.delay

@Composable
fun SpocDetailScreen(
    onBackClick: () -> Unit,
    onParticipantClick: ((String) -> Unit)? = null,
    viewModel: SpocDetailViewModel = hiltViewModel()
) {
    val spocData by viewModel.spocData.collectAsStateWithLifecycle()
    val matrix by viewModel.matrix.collectAsStateWithLifecycle()
    val lastUpdated by viewModel.lastUpdated.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()

    BackHandler(onBack = onBackClick)

    val spoc = spocData?.spoc
    val spocName = spoc?.name ?: "SPOC ${viewModel.spocId}"
    val spocPhone = spoc?.phoneNumber

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                viewModel.refresh()
                delay(20_000)
            }
        }
    }

    SpocDetailContent(
        spocId = viewModel.spocId,
        spocName = spocName,
        spocPhone = spocPhone,
        teams = spocData?.teams ?: emptyList(),
        matrix = matrix,
        currentRole = currentRole,
        lastUpdated = lastUpdated,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        onBackClick = onBackClick,
        onParticipantClick = onParticipantClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpocDetailContent(
    spocId: String,
    spocName: String,
    spocPhone: String?,
    teams: List<com.example.eventattendance.domain.model.TeamWithMembers>,
    matrix: com.example.eventattendance.domain.model.AttendanceMatrix?,
    currentRole: UserRole,
    lastUpdated: Long?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    onParticipantClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = spocName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SPOC ID: $spocId",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!spocPhone.isNullOrBlank()) {
                            CallButton(
                                phoneNumber = spocPhone,
                                currentRole = currentRole,
                                size = 30.dp,
                                iconSize = 15.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                },
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
                    IconButton(onClick = onRefresh) {
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TeamProgressContent(
                spocId = spocId,
                teams = teams,
                matrix = matrix,
                currentRole = currentRole,
                lastUpdated = lastUpdated,
                spocPhone = spocPhone,
                onParticipantClick = onParticipantClick
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun SpocDetailContentLightPreview() {
    CheckdTheme(darkTheme = false) {
        SpocDetailContent(
            spocId = "SPOC-01",
            spocName = "Jane Doe",
            spocPhone = "9876543210",
            teams = emptyList(),
            matrix = null,
            currentRole = UserRole.Admin,
            lastUpdated = System.currentTimeMillis(),
            isRefreshing = false,
            onRefresh = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SpocDetailContentDarkPreview() {
    CheckdTheme(darkTheme = true) {
        SpocDetailContent(
            spocId = "SPOC-01",
            spocName = "Jane Doe",
            spocPhone = "9876543210",
            teams = emptyList(),
            matrix = null,
            currentRole = UserRole.Admin,
            lastUpdated = System.currentTimeMillis(),
            isRefreshing = false,
            onRefresh = {},
            onBackClick = {}
        )
    }
}
