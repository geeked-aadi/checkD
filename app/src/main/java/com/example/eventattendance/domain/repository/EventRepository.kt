package com.example.eventattendance.domain.repository

import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.domain.model.EventWithStats
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getEventsWithStatsFlow(): Flow<List<EventWithStats>>
    fun getEventByIdFlow(eventId: String): Flow<Event?>
    suspend fun getEventById(eventId: String): Event?
    suspend fun createEvent(event: Event): Result<Event>
    suspend fun updateEventStatus(eventId: String, isOpen: Boolean): Result<Unit>
    suspend fun updateTimeWindow(eventId: Int, startsFrom: String, endsOn: String): Result<Unit>
    suspend fun refreshEventsFromCloud(): Result<Unit>
}
