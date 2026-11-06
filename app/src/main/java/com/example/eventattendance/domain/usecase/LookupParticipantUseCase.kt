package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.model.CachedParticipant
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import javax.inject.Inject

class LookupParticipantUseCase @Inject constructor(
    private val participantCacheRepository: ParticipantCacheRepository
) {
    suspend operator fun invoke(pId: String): CachedParticipant? {
        val cleanId = pId.trim()
        if (cleanId.isBlank()) return null
        var participant = participantCacheRepository.getParticipant(cleanId)
        if (participant == null && participantCacheRepository.isCacheEmpty()) {
            participantCacheRepository.refreshParticipants()
            participant = participantCacheRepository.getParticipant(cleanId)
        }
        return participant
    }
}
