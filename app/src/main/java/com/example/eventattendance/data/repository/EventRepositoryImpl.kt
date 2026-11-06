package com.example.eventattendance.data.repository

import com.example.eventattendance.data.remote.datasource.EventRemoteDataSource
import com.example.eventattendance.data.remote.dto.EventDto
import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.domain.model.EventWithStats
import com.example.eventattendance.domain.repository.AttendanceRepository
import com.example.eventattendance.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val attendanceRepository: AttendanceRepository
) : EventRepository {

    override fun getEventsWithStatsFlow(): Flow<List<EventWithStats>> {
        return attendanceRepository.eventsWithStatsFlow
    }

    override fun getEventByIdFlow(eventId: String): Flow<Event?> {
        val id = eventId.toIntOrNull() ?: 0
        return attendanceRepository.getEventAttendanceFlow(id).map { detail ->
            detail?.let {
                Event(
                    id = it.eventId.toString(),
                    eventId = it.eventId,
                    name = it.eventName,
                    startsFrom = it.startsFrom,
                    endsOn = it.endsOn
                )
            }
        }
    }

    override suspend fun getEventById(eventId: String): Event? {
        val id = eventId.toIntOrNull() ?: return null
        return try {
            eventRemoteDataSource.getEventById(id)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createEvent(event: Event): Result<Event> {
        return try {
            val eventId = if (event.eventId > 0) {
                event.eventId
            } else {
                eventRemoteDataSource.getNextEventId()
            }

            val eventDto = EventDto(
                eventId = eventId,
                eventName = event.name,
                startsFrom = event.startsFrom,
                endsOn = event.endsOn
            )

            val created = eventRemoteDataSource.createEvent(eventDto)
            attendanceRepository.syncAllEventsAttendance(forcePull = true)
            Result.success(created.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEventStatus(eventId: String, isOpen: Boolean): Result<Unit> {
        return try {
            attendanceRepository.syncAllEventsAttendance(forcePull = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTimeWindow(eventId: Int, startsFrom: String, endsOn: String): Result<Unit> {
        return try {
            eventRemoteDataSource.updateEventTimeWindow(eventId, startsFrom, endsOn)
            attendanceRepository.syncAllEventsAttendance(forcePull = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshEventsFromCloud(): Result<Unit> {
        return attendanceRepository.syncAllEventsAttendance(forcePull = true).map { }
    }
}
