package com.example.eventattendance.domain.model

data class Spoc(
    val spocId: String,
    val name: String,
    val phoneNumber: String? = null
)

data class Team(
    val teamId: String,
    val teamName: String,
    val teamLeaderId: String? = null,
    val spocId: String? = null
)

data class TeamMember(
    val pId: String,
    val name: String,
    val phoneNumber: String? = null,
    val isLeader: Boolean = false
)

data class TeamWithMembers(
    val teamId: String,
    val teamName: String,
    val teamLeaderId: String? = null,
    val teamLeaderName: String? = null,
    val spocId: String? = null,
    val members: List<TeamMember> = emptyList()
)

data class SpocWithTeams(
    val spoc: Spoc,
    val teams: List<Team> = emptyList()
)

data class SpocDashboardData(
    val spoc: Spoc,
    val teams: List<TeamWithMembers> = emptyList()
)

data class AdminDirectoryData(
    val spocs: List<SpocWithTeams> = emptyList(),
    val teams: List<TeamWithMembers> = emptyList(),
    val participants: List<Participant> = emptyList()
)
