package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.model.CachedParticipant
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import javax.inject.Inject

class VerifyParticipantUseCase @Inject constructor(
    private val participantCacheRepository: ParticipantCacheRepository
) {
    suspend operator fun invoke(token: String): CachedParticipant? {
        val cleanId = token.trim()
        if (cleanId.isBlank()) return null

        var participant = participantCacheRepository.getParticipant(cleanId)
        if (participant == null && participantCacheRepository.isCacheEmpty()) {
            participantCacheRepository.refreshParticipants()
            participant = participantCacheRepository.getParticipant(cleanId)
        }
        return participant
    }
}
