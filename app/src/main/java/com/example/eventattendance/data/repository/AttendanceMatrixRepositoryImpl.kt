package com.example.eventattendance.data.repository

import com.example.eventattendance.data.remote.datasource.RpcRemoteDataSource
import com.example.eventattendance.di.IoDispatcher
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
        runCatching {
            val response = rpcRemoteDataSource.getAttendanceMatrix()
            val mappedEvents = response.events.map { eventDto ->
                EventAttendance(
                    eventId = eventDto.eventId,
                    eventName = eventDto.eventName ?: "Event ${eventDto.eventId}",
                    startsFrom = eventDto.startsFrom,
                    endsOn = eventDto.endsOn,
                    presentPIds = eventDto.presentPIds.toSet()
                )
            }
            _matrixFlow.value = AttendanceMatrix(
                status = response.status,
                totalParticipants = response.totalParticipants,
                events = mappedEvents
            )
            _lastUpdated.value = System.currentTimeMillis()
            Unit
        }
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
}
