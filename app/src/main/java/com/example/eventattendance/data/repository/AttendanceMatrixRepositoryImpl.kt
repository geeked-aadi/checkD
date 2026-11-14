package com.example.eventattendance.data.repository

import com.example.eventattendance.data.remote.datasource.RpcRemoteDataSource
import com.example.eventattendance.di.IoDispatcher
import com.example.eventattendance.domain.calculator.ProgressCalculator
import com.example.eventattendance.domain.model.AttendanceMatrix
import com.example.eventattendance.domain.model.EventAttendance
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceMatrixRepositoryImpl @Inject constructor(
    private val rpcRemoteDataSource: RpcRemoteDataSource,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AttendanceMatrixRepository {

    private val _matrixFlow = MutableStateFlow<AttendanceMatrix?>(null)
    override val matrixFlow: StateFlow<AttendanceMatrix?> = _matrixFlow.asStateFlow()

    private val _lastUpdated = MutableStateFlow<Long?>(null)
    override val lastUpdated: StateFlow<Long?> = _lastUpdated.asStateFlow()

    override suspend fun refresh(): Result<Unit> = withContext(ioDispatcher) {
        // 1. Primary: Try get_attendance_matrix RPC
        val matrixRpcResult = runCatching {
            val response = rpcRemoteDataSource.getAttendanceMatrix()
            val mappedEvents = response.events.map { eventDto ->
                EventAttendance(
                    eventId = eventDto.eventId,
                    eventName = eventDto.eventName ?: "Event ${eventDto.eventId}",
                    startsFrom = eventDto.startsFrom,
                    endsOn = eventDto.endsOn,
                    presentPIds = eventDto.presentPIds.toSet()
                )
            }.let { ProgressCalculator.sortEventsByTime(it, { e -> e.startsFrom }, { e -> e.endsOn }, { e -> e.eventId }) }
            _matrixFlow.value = AttendanceMatrix(
                status = response.status,
                totalParticipants = response.totalParticipants,
                events = mappedEvents
            )
            _lastUpdated.value = System.currentTimeMillis()
            Unit
        }

        if (matrixRpcResult.isSuccess) {
            return@withContext matrixRpcResult
        }

        // 2. Fallback: If get_attendance_matrix fails (e.g. 401 unauthenticated / permission denied),
        // use sync_all_events_attendance RPC which provides the exact same events and attendance details
        val syncAllRpcResult = runCatching {
            val syncResponse = rpcRemoteDataSource.syncAllEventsAttendance()
            val mappedEvents = syncResponse.events.map { item ->
                EventAttendance(
                    eventId = item.eventId,
                    eventName = item.eventName ?: "Event ${item.eventId}",
                    startsFrom = item.startsFrom,
                    endsOn = item.endsOn,
                    presentPIds = item.presentPIds.toSet()
                )
            }.let { ProgressCalculator.sortEventsByTime(it, { e -> e.startsFrom }, { e -> e.endsOn }, { e -> e.eventId }) }
            _matrixFlow.value = AttendanceMatrix(
                status = syncResponse.status,
                totalParticipants = syncResponse.totalParticipants ?: 0,
                events = mappedEvents
            )
            _lastUpdated.value = System.currentTimeMillis()
            Unit
        }

        if (syncAllRpcResult.isSuccess) {
            return@withContext syncAllRpcResult
        }

        matrixRpcResult
    }

    override fun addLocalScan(eventId: Int, pId: String) {
        _matrixFlow.update { currentMatrix ->
            if (currentMatrix == null) return@update null
            val updatedEvents = currentMatrix.events.map { event ->
                if (event.eventId == eventId) {
                    event.copy(presentPIds = event.presentPIds + pId)
                } else {
                    event
                }
            }
            currentMatrix.copy(events = updatedEvents)
        }
    }

    override fun updateLocalEvent(eventId: Int, name: String?, startsFrom: String, endsOn: String) {
        val trimmedName = if (!name.isNullOrBlank()) name.trim() else null
        _matrixFlow.update { currentMatrix ->
            if (currentMatrix == null) {
                AttendanceMatrix(
                    status = "success",
                    totalParticipants = 0,
                    events = listOf(
                        EventAttendance(
                            eventId = eventId,
                            eventName = trimmedName ?: "Event $eventId",
                            startsFrom = startsFrom,
                            endsOn = endsOn,
                            presentPIds = emptySet()
                        )
                    )
                )
            } else {
                val exists = currentMatrix.events.any { it.eventId == eventId }
                val updatedEvents = if (exists) {
                    currentMatrix.events.map { event ->
                        if (event.eventId == eventId) {
                            event.copy(
                                eventName = trimmedName ?: event.eventName,
                                startsFrom = startsFrom,
                                endsOn = endsOn
                            )
                        } else {
                            event
                        }
                    }
                } else {
                    currentMatrix.events + EventAttendance(
                        eventId = eventId,
                        eventName = trimmedName ?: "Event $eventId",
                        startsFrom = startsFrom,
                        endsOn = endsOn,
                        presentPIds = emptySet()
                    )
                }
                val sortedEvents = ProgressCalculator.sortEventsByTime(
                    updatedEvents,
                    { it.startsFrom },
                    { it.endsOn },
                    { it.eventId }
                )
                currentMatrix.copy(events = sortedEvents)
            }
        }
        _lastUpdated.value = System.currentTimeMillis()
    }

    override fun addLocalEvent(event: EventAttendance) {
        _matrixFlow.update { currentMatrix ->
            if (currentMatrix == null) {
                AttendanceMatrix(
                    status = "success",
                    totalParticipants = 0,
                    events = listOf(event)
                )
            } else {
                val existing = currentMatrix.events.filterNot { it.eventId == event.eventId }
                val sortedEvents = ProgressCalculator.sortEventsByTime(
                    existing + event,
                    { it.startsFrom },
                    { it.endsOn },
                    { it.eventId }
                )
                currentMatrix.copy(events = sortedEvents)
            }
        }
    }

    override fun clearMatrix() {
        _matrixFlow.value = null
        _lastUpdated.value = null
    }
}

