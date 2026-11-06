package com.example.eventattendance.domain.repository

import com.example.eventattendance.domain.model.CachedParticipant
import kotlinx.coroutines.flow.StateFlow

interface ParticipantCacheRepository {
    val participantsFlow: StateFlow<Map<String, CachedParticipant>>
    suspend fun initializeCache()
    suspend fun refreshParticipants(): Result<Unit>
    fun getParticipant(pId: String): CachedParticipant?
    fun getAllParticipants(): List<CachedParticipant>
    fun getByTeam(teamId: String): List<CachedParticipant>
    fun getBySpoc(spocId: String): List<CachedParticipant>
    fun getMemberPIds(spocId: String): Set<String>
    fun isCacheEmpty(): Boolean
}
