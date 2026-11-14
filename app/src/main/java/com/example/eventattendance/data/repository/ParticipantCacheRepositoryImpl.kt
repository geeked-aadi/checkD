package com.example.eventattendance.data.repository

import android.content.Context
import com.example.eventattendance.data.remote.datasource.RpcRemoteDataSource
import com.example.eventattendance.data.remote.dto.ParticipantDto
import com.example.eventattendance.data.remote.dto.SpocDto
import com.example.eventattendance.data.remote.dto.TeamDto
import com.example.eventattendance.di.IoDispatcher
import com.example.eventattendance.domain.model.CachedParticipant
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParticipantCacheRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val rpcRemoteDataSource: RpcRemoteDataSource,
    private val supabaseClient: SupabaseClient,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ParticipantCacheRepository {

    private val cacheFile = File(context.filesDir, "participants_cache.json")

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        prettyPrint = false
    }

    private val _participantsMap = MutableStateFlow<Map<String, CachedParticipant>>(emptyMap())
    override val participantsFlow: StateFlow<Map<String, CachedParticipant>> = _participantsMap.asStateFlow()

    @Volatile
    private var byTeamIndex: Map<String, List<CachedParticipant>> = emptyMap()

    @Volatile
    private var bySpocIndex: Map<String, List<CachedParticipant>> = emptyMap()

    private fun rebuildIndexes(list: Collection<CachedParticipant>) {
        _participantsMap.value = list.associateBy { it.pId }
        byTeamIndex = list.filter { !it.teamId.isNullOrBlank() }.groupBy { it.teamId!! }
        bySpocIndex = list.filter { !it.spocId.isNullOrBlank() }.groupBy { it.spocId!! }
    }

    override suspend fun initializeCache(): Unit = withContext(ioDispatcher) {
        // 1. Fast initial load from disk cache for offline availability
        if (cacheFile.exists()) {
            runCatching {
                val content = cacheFile.readText()
                if (content.isNotBlank()) {
                    val list = json.decodeFromString<List<CachedParticipant>>(content)
                    rebuildIndexes(list)
                }
            }
        }

        // 2. Always fetch fresh participants from remote RPC (get_all_participants) on launch
        refreshParticipants()
    }

    override suspend fun refreshParticipants(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val rpcResult = runCatching { rpcRemoteDataSource.getAllParticipants() }
            val rpcParticipants = rpcResult.getOrNull()?.participants ?: emptyList()

            val initialList = rpcParticipants.map { item ->
                CachedParticipant(
                    pId = item.pId,
                    name = item.resolvedName(),
                    phonenumber = item.phonenumber,
                    teamId = item.teamId,
                    teamName = item.resolvedTeamName(),
                    teamLeader = item.teamLeader,
                    spocId = item.spocId,
                    spocName = item.resolvedSpocName(),
                    spocPhonenumber = item.spocPhonenumber
                )
            }

            // Check if any fields need enrichment (missing name, team, or spoc, or if RPC failed completely)
            val needsEnrichment = (initialList.isEmpty() && rpcResult.isFailure) ||
                initialList.any {
                    it.name.isNullOrBlank() ||
                    (it.teamName.isNullOrBlank() && !it.teamId.isNullOrBlank()) ||
                    (it.spocName.isNullOrBlank() && !it.spocId.isNullOrBlank()) ||
                    (it.teamName.isNullOrBlank() && it.spocName.isNullOrBlank())
                }

            val finalList = if (needsEnrichment) {
                // If RPC was completely empty/failed, query participants table directly
                val directParticipants = if (initialList.isEmpty()) {
                    runCatching {
                        supabaseClient.from("participants").select().decodeList<ParticipantDto>()
                    }.getOrDefault(emptyList())
                } else {
                    emptyList()
                }

                // Query teams table
                val directTeams = runCatching {
                    supabaseClient.from("teams").select().decodeList<TeamDto>()
                }.getOrDefault(emptyList()).associateBy { it.teamId }

                // Query spoc table
                val directSpocs = runCatching {
                    supabaseClient.from("spoc").select().decodeList<SpocDto>()
                }.getOrDefault(emptyList()).associateBy { it.spocId }

                // If names are missing in initial list, query direct participants table
                val directParticipantsMap = if (initialList.any { it.name.isNullOrBlank() }) {
                    runCatching {
                        supabaseClient.from("participants").select().decodeList<ParticipantDto>()
                    }.getOrDefault(emptyList()).associateBy { it.pId }
                } else {
                    directParticipants.associateBy { it.pId }
                }

                val baseList = if (initialList.isNotEmpty()) {
                    initialList
                } else {
                    directParticipants.map { p ->
                        CachedParticipant(
                            pId = p.pId,
                            name = p.name,
                            phonenumber = p.phonenumber,
                            teamId = p.teamId,
                            teamName = null,
                            teamLeader = null,
                            spocId = null,
                            spocName = null,
                            spocPhonenumber = null
                        )
                    }
                }

                baseList.map { p ->
                    val directP = directParticipantsMap[p.pId]
                    val effectiveTeamId = p.teamId ?: directP?.teamId
                    val team = effectiveTeamId?.let { directTeams[it] }
                    val effectiveSpocId = p.spocId ?: team?.spocId
                    val spoc = effectiveSpocId?.let { directSpocs[it] }

                    p.copy(
                        name = p.name?.takeIf { it.isNotBlank() } ?: directP?.name?.takeIf { it.isNotBlank() },
                        phonenumber = p.phonenumber ?: directP?.phonenumber,
                        teamId = effectiveTeamId,
                        teamName = p.teamName?.takeIf { it.isNotBlank() } ?: team?.teamName,
                        teamLeader = p.teamLeader?.takeIf { it.isNotBlank() } ?: team?.teamLeader,
                        spocId = effectiveSpocId,
                        spocName = p.spocName?.takeIf { it.isNotBlank() } ?: spoc?.name,
                        spocPhonenumber = p.spocPhonenumber ?: spoc?.phonenumber
                    )
                }
            } else {
                initialList
            }

            // Always encode and persist even if empty (handles truncated or reset DB)
            val jsonString = json.encodeToString(finalList)
            cacheFile.writeText(jsonString)
            rebuildIndexes(finalList)
            Unit
        }
    }

    override fun getParticipant(pId: String): CachedParticipant? {
        return _participantsMap.value[pId.trim()]
    }

    override fun getAllParticipants(): List<CachedParticipant> {
        return _participantsMap.value.values.toList()
    }

    override fun getByTeam(teamId: String): List<CachedParticipant> {
        return byTeamIndex[teamId.trim()] ?: emptyList()
    }

    override fun getBySpoc(spocId: String): List<CachedParticipant> {
        return bySpocIndex[spocId.trim()] ?: emptyList()
    }

    override fun getMemberPIds(spocId: String): Set<String> {
        return getBySpoc(spocId).map { it.pId }.toSet()
    }

    override fun isCacheEmpty(): Boolean {
        return _participantsMap.value.isEmpty()
    }

    override suspend fun clearCache(): Unit = withContext(ioDispatcher) {
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
        rebuildIndexes(emptyList())
    }
}

