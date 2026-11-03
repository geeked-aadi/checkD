package com.example.eventattendance.domain.repository

import com.example.eventattendance.domain.model.Participant
import kotlinx.coroutines.flow.Flow

interface ParticipantRepository {
    fun getAllParticipantsFlow(): Flow<List<Participant>>
    fun searchParticipantsFlow(query: String): Flow<List<Participant>>
    suspend fun getParticipantByQrToken(qrToken: String): Participant?
    suspend fun isParticipantEligible(eventId: String, participantId: String): Boolean
}
