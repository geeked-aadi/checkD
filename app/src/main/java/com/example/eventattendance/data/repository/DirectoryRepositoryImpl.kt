package com.example.eventattendance.data.repository

import com.example.eventattendance.data.remote.dto.SpocDto
import com.example.eventattendance.data.remote.dto.TeamDto
import com.example.eventattendance.di.IoDispatcher
import com.example.eventattendance.domain.model.AdminDirectoryData
import com.example.eventattendance.domain.model.Spoc
import com.example.eventattendance.domain.model.SpocDashboardData
import com.example.eventattendance.domain.model.SpocWithTeams
import com.example.eventattendance.domain.model.Team
import com.example.eventattendance.domain.model.TeamMember
import com.example.eventattendance.domain.model.TeamWithMembers
import com.example.eventattendance.domain.repository.DirectoryRepository
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DirectoryRepositoryImpl @Inject constructor(
    private val participantCacheRepository: ParticipantCacheRepository,
    private val supabaseClient: SupabaseClient,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : DirectoryRepository {

    override suspend fun getSpocDashboardData(spocId: String): Result<SpocDashboardData> = withContext(ioDispatcher) {
        runCatching {
            if (participantCacheRepository.isCacheEmpty()) {
                participantCacheRepository.refreshParticipants()
            }

            val all = participantCacheRepository.getAllParticipants()
            val spocParticipants = all.filter { it.spocId == spocId }

            val directSpoc = runCatching {
                supabaseClient.from("spoc").select {
                    filter { eq("spoc_id", spocId) }
                }.decodeSingleOrNull<SpocDto>()
            }.getOrNull()

            val firstSpocItem = spocParticipants.firstOrNull()
            val spoc = Spoc(
                spocId = spocId,
                name = directSpoc?.name ?: firstSpocItem?.spocName ?: "SPOC $spocId",
                phoneNumber = directSpoc?.phonenumber ?: firstSpocItem?.spocPhonenumber
            )

            // Direct teams for this SPOC so teams with 0 participants still show up
            val directTeams = runCatching {
                supabaseClient.from("teams").select {
                    filter { eq("spoc_id", spocId) }
                }.decodeList<TeamDto>()
            }.getOrDefault(emptyList())

            val teamsGrouped = spocParticipants.filter { !it.teamId.isNullOrBlank() }
                .groupBy { it.teamId!! }

            val allTeamIds = (directTeams.map { it.teamId } + teamsGrouped.keys).distinct()

            val teamsWithMembers = allTeamIds.map { teamId ->
                val directTeam = directTeams.find { it.teamId == teamId }
                val membersList = teamsGrouped[teamId] ?: emptyList()
                val teamName = directTeam?.teamName
                    ?: membersList.firstOrNull { !it.teamName.isNullOrBlank() }?.teamName
                    ?: teamId
                val leaderId = directTeam?.teamLeader
                    ?: membersList.firstOrNull { !it.teamLeader.isNullOrBlank() }?.teamLeader
                val leaderName = all.find { it.pId == leaderId }?.name
                val members = membersList.map { p ->
                    TeamMember(
                        pId = p.pId,
                        name = p.name ?: p.pId,
                        phoneNumber = p.phonenumber,
                        isLeader = (p.pId == leaderId)
                    )
                }
                TeamWithMembers(
                    teamId = teamId,
                    teamName = teamName,
                    teamLeaderId = leaderId,
                    teamLeaderName = leaderName,
                    spocId = spocId,
                    members = members
                )
            }

            SpocDashboardData(
                spoc = spoc,
                teams = teamsWithMembers
            )
        }
    }

    override suspend fun getAdminDirectoryData(): Result<AdminDirectoryData> = withContext(ioDispatcher) {
        runCatching {
            if (participantCacheRepository.isCacheEmpty()) {
                participantCacheRepository.refreshParticipants()
            }

            val all = participantCacheRepository.getAllParticipants()

            // Read spoc and teams directly so SPOCs with 0 teams and teams with 0 participants appear
            val directSpocs = runCatching {
                supabaseClient.from("spoc").select().decodeList<SpocDto>()
            }.getOrDefault(emptyList())

            val directTeams = runCatching {
                supabaseClient.from("teams").select().decodeList<TeamDto>()
            }.getOrDefault(emptyList())

            // 1. All SPOCs with their teams
            val spocsFromCache = all.filter { !it.spocId.isNullOrBlank() }.groupBy { it.spocId!! }
            val allSpocIds = (directSpocs.map { it.spocId } + spocsFromCache.keys).distinct()

            val spocsWithTeams = allSpocIds.map { spocId ->
                val direct = directSpocs.find { it.spocId == spocId }
                val cacheList = spocsFromCache[spocId] ?: emptyList()
                val spoc = Spoc(
                    spocId = spocId,
                    name = direct?.name ?: cacheList.firstOrNull { !it.spocName.isNullOrBlank() }?.spocName ?: "SPOC $spocId",
                    phoneNumber = direct?.phonenumber ?: cacheList.firstOrNull()?.spocPhonenumber
                )

                val teamsForSpoc = directTeams.filter { it.spocId == spocId }.map { it.toDomain() }.ifEmpty {
                    cacheList.filter { !it.teamId.isNullOrBlank() }
                        .groupBy { it.teamId!! }
                        .map { (teamId, teamParticipants) ->
                            Team(
                                teamId = teamId,
                                teamName = teamParticipants.firstOrNull { !it.teamName.isNullOrBlank() }?.teamName ?: teamId,
                                teamLeaderId = teamParticipants.firstOrNull()?.teamLeader,
                                spocId = spocId
                            )
                        }
                }

                SpocWithTeams(
                    spoc = spoc,
                    teams = teamsForSpoc
                )
            }

            // 2. Teams with their members
            val teamsFromCache = all.filter { !it.teamId.isNullOrBlank() }.groupBy { it.teamId!! }
            val allTeamIds = (directTeams.map { it.teamId } + teamsFromCache.keys).distinct()

            val teamsWithMembers = allTeamIds.map { teamId ->
                val directTeam = directTeams.find { it.teamId == teamId }
                val membersList = teamsFromCache[teamId] ?: emptyList()
                val teamName = directTeam?.teamName
                    ?: membersList.firstOrNull { !it.teamName.isNullOrBlank() }?.teamName
                    ?: teamId
                val leaderId = directTeam?.teamLeader
                    ?: membersList.firstOrNull { !it.teamLeader.isNullOrBlank() }?.teamLeader
                val leaderName = all.find { it.pId == leaderId }?.name
                val spocId = directTeam?.spocId ?: membersList.firstOrNull()?.spocId
                val members = membersList.map { p ->
                    TeamMember(
                        pId = p.pId,
                        name = p.name ?: p.pId,
                        phoneNumber = p.phonenumber,
                        isLeader = (p.pId == leaderId)
                    )
                }
                TeamWithMembers(
                    teamId = teamId,
                    teamName = teamName,
                    teamLeaderId = leaderId,
                    teamLeaderName = leaderName,
                    spocId = spocId,
                    members = members
                )
            }

            // 3. All participants
            val participants = all.map { it.toParticipant() }

            AdminDirectoryData(
                spocs = spocsWithTeams,
                teams = teamsWithMembers,
                participants = participants
            )
        }
    }
}
