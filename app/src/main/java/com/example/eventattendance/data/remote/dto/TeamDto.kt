package com.example.eventattendance.data.remote.dto

import com.example.eventattendance.domain.model.Team
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamDto(
    @SerialName("team_id")
    val teamId: String,
    @SerialName("team_name")
    val teamName: String? = null,
    @SerialName("team_leader")
    val teamLeader: String? = null,
    @SerialName("spoc_id")
    val spocId: String? = null
) {
    fun toDomain(): Team = Team(
        teamId = teamId,
        teamName = teamName ?: teamId,
        teamLeaderId = teamLeader,
        spocId = spocId
    )
}
