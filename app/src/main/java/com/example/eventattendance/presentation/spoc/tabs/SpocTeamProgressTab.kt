package com.example.eventattendance.presentation.spoc.tabs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.eventattendance.domain.model.AttendanceMatrix
import com.example.eventattendance.domain.model.SpocDashboardData
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.presentation.spoc.components.TeamProgressContent

@Composable
fun SpocTeamProgressTab(
    spocId: String,
    spocData: SpocDashboardData?,
    matrix: AttendanceMatrix?,
    currentRole: UserRole,
    lastUpdated: Long?,
    modifier: Modifier = Modifier,
    onParticipantClick: ((String) -> Unit)? = null
) {
    val teams = spocData?.teams ?: emptyList()

    TeamProgressContent(
        spocId = spocId,
        teams = teams,
        matrix = matrix,
        currentRole = currentRole,
        lastUpdated = lastUpdated,
        onParticipantClick = onParticipantClick,
        modifier = modifier
    )
}
