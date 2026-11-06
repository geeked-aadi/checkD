package com.example.eventattendance.data.repository

import android.content.Context
import com.example.eventattendance.data.remote.datasource.RpcRemoteDataSource
import com.example.eventattendance.di.IoDispatcher
import com.example.eventattendance.domain.model.CachedParticipant
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
        if (cacheFile.exists()) {
            runCatching {
                val content = cacheFile.readText()
                if (content.isNotBlank()) {
                    val list = json.decodeFromString<List<CachedParticipant>>(content)
                    rebuildIndexes(list)
                }
            }
        }

        // If local file was missing or empty, fetch from remote RPC once
        if (_participantsMap.value.isEmpty()) {
            refreshParticipants()
        }
    }

    override suspend fun refreshParticipants(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val response = rpcRemoteDataSource.getAllParticipants()
            val list = response.participants.map { item ->
                CachedParticipant(
                    pId = item.pId,
                    name = item.name,
                    phonenumber = item.phonenumber,
                    teamId = item.teamId,
                    teamName = item.teamName,
                    teamLeader = item.teamLeader,
                    spocId = item.spocId,
                    spocName = item.spocName,
                    spocPhonenumber = item.spocPhonenumber
                )
            }
            if (list.isNotEmpty()) {
                val jsonString = json.encodeToString(list)
                cacheFile.writeText(jsonString)
                rebuildIndexes(list)
            }
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
}
