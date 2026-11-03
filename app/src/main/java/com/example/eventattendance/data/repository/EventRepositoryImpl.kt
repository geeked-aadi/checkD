package com.example.eventattendance.data.repository

import com.example.eventattendance.data.local.AppDatabase
import com.example.eventattendance.data.local.entity.EventEntity
import com.example.eventattendance.data.remote.datasource.EventRemoteDataSource
import com.example.eventattendance.data.remote.dto.EventDto
import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.domain.model.EventWithStats
import com.example.eventattendance.domain.repository.EventRepository
import com.example.eventattendance.sync.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val syncManager: SyncManager
) : EventRepository {

    override fun getEventsWithStatsFlow(): Flow<List<EventWithStats>> {
        val eventsFlow = database.eventDao().getAllEventsFlow()

        return eventsFlow.map { events ->
            events.map { eventEntity ->
                val scannedCount = database.attendanceDao().getScannedCount(eventEntity.id)
                val eligibleCount = database.eligibilityDao().getEligibleCount(eventEntity.id)
                EventWithStats(
                    event = eventEntity.toDomain(),
                    scannedCount = scannedCount,
                    totalEligibleCount = if (eligibleCount > 0) eligibleCount else 200,
                    pendingSyncCount = 0
                )
            }
        }
    }

    override fun getEventByIdFlow(eventId: String): Flow<Event?> {
        return database.eventDao().getEventByIdFlow(eventId).map { it?.toDomain() }
    }

    override suspend fun getEventById(eventId: String): Event? {
        return database.eventDao().getEventById(eventId)?.toDomain()
    }

    override suspend fun createEvent(event: Event): Result<Event> {
        return try {
            val eventId = event.id.ifBlank { UUID.randomUUID().toString() }
            val eventDto = EventDto(
                id = eventId,
                name = event.name,
                description = event.description,
                eventDate = event.eventDate,
                startTime = event.startTime,
                endTime = event.endTime,
                status = event.status.value
            )

            // Try creating on remote
            try {
                eventRemoteDataSource.createEvent(eventDto)
            } catch (e: Exception) {
                // If offline, save locally
            }

            database.eventDao().insertEvent(eventDto.toEntity())
            Result.success(eventDto.toEntity().toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEventStatus(eventId: String, isOpen: Boolean): Result<Unit> {
        val status = if (isOpen) "open" else "closed"
        return try {
            try {
                eventRemoteDataSource.updateEventStatus(eventId, status)
            } catch (e: Exception) {
                // Offline fallback
            }
            val existing = database.eventDao().getEventById(eventId)
            if (existing != null) {
                database.eventDao().insertEvent(existing.copy(status = status))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshEventsFromCloud(): Result<Unit> {
        return syncManager.syncAll(forcePull = true)
    }
}
