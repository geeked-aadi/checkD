package com.example.eventattendance.data.repository

import com.example.eventattendance.domain.model.Participant
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import com.example.eventattendance.domain.repository.ParticipantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParticipantRepositoryImpl @Inject constructor(
    private val participantCacheRepository: ParticipantCacheRepository
) : ParticipantRepository {

    override fun getAllParticipantsFlow(): Flow<List<Participant>> {
        return participantCacheRepository.participantsFlow.map { map ->
            map.values.map { it.toParticipant() }
        }
    }

    override fun searchParticipantsFlow(query: String): Flow<List<Participant>> {
        return getAllParticipantsFlow().map { list ->
            if (query.isBlank()) {
                list
            } else {
                val q = query.trim().lowercase()
                list.filter {
                    it.name.lowercase().contains(q) ||
                    it.id.lowercase().contains(q) ||
                    it.teamId?.lowercase()?.contains(q) == true
                }
            }
        }
    }

    override suspend fun getParticipantByQrToken(qrToken: String): Participant? {
        return participantCacheRepository.getParticipant(qrToken.trim())?.toParticipant()
    }

    override suspend fun isParticipantEligible(eventId: String, participantId: String): Boolean {
        return participantCacheRepository.getParticipant(participantId.trim()) != null
    }
}
