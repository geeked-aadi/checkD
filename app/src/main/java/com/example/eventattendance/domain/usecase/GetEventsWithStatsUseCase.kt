package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.model.EventWithStats
import com.example.eventattendance.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventsWithStatsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(): Flow<List<EventWithStats>> {
        return eventRepository.getEventsWithStatsFlow()
    }
}
