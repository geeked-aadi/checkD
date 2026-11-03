package com.example.eventattendance.data.repository

import com.example.eventattendance.data.local.AppDatabase
import com.example.eventattendance.domain.model.Participant
import com.example.eventattendance.domain.repository.ParticipantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParticipantRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : ParticipantRepository {

    override fun getAllParticipantsFlow(): Flow<List<Participant>> {
        return database.participantDao().getAllParticipantsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun searchParticipantsFlow(query: String): Flow<List<Participant>> {
        return database.participantDao().searchParticipants(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getParticipantByQrToken(qrToken: String): Participant? {
        return database.participantDao().getParticipantByQrToken(qrToken)?.toDomain()
    }

    override suspend fun isParticipantEligible(eventId: String, participantId: String): Boolean {
        return database.eligibilityDao().isParticipantEligible(eventId, participantId)
    }
}
