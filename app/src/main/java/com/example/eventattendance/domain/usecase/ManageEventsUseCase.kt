package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.domain.repository.EventRepository
import javax.inject.Inject

class ManageEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend fun createEvent(event: Event): Result<Event> {
        return eventRepository.createEvent(event)
    }

    suspend fun toggleEventStatus(eventId: String, isOpen: Boolean): Result<Unit> {
        return eventRepository.updateEventStatus(eventId, isOpen)
    }

    suspend fun updateTimeWindow(eventId: Int, startsFrom: String, endsOn: String): Result<Unit> {
        return eventRepository.updateTimeWindow(eventId, startsFrom, endsOn)
    }
}
