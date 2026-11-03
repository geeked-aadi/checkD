package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.model.Participant
import com.example.eventattendance.domain.repository.ParticipantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchParticipantsUseCase @Inject constructor(
    private val participantRepository: ParticipantRepository
) {
    operator fun invoke(query: String): Flow<List<Participant>> {
        return if (query.isBlank()) {
            participantRepository.getAllParticipantsFlow()
        } else {
            participantRepository.searchParticipantsFlow(query.trim())
        }
    }
}
