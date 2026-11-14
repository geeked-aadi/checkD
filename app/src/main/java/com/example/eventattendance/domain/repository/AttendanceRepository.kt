package com.example.eventattendance.domain.repository

import com.example.eventattendance.data.remote.dto.MarkAttendanceResponseDto
import com.example.eventattendance.domain.model.AttendanceRecord
import com.example.eventattendance.domain.model.EventDetailData
import com.example.eventattendance.domain.model.EventWithStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AttendanceRepository {
    val eventsWithStatsFlow: StateFlow<List<EventWithStats>>
    suspend fun syncAllEventsAttendance(forcePull: Boolean = false): Result<List<EventWithStats>>
    suspend fun syncEventAttendance(eventId: Int): Result<EventDetailData>
    fun getEventAttendanceFlow(eventId: Int): Flow<EventDetailData?>
    fun updateLocalEvent(eventId: Int, name: String?, startsFrom: String, endsOn: String)
    fun updateLocalEventTimeWindow(eventId: Int, name: String?, startsFrom: String, endsOn: String)
    suspend fun markAttendance(eventId: Int, pIds: List<String>, role: String): Result<MarkAttendanceResponseDto>
    fun recordScanSuccess(
        eventId: Int,
        participantId: String,
        name: String,
        teamId: String?,
        teamName: String?,
        currentTime: String,
        progress: String?,
        newAttendance: Int?,
        total: Int?
    )
    fun recordBulkScanSuccess(
        eventId: Int,
        markedAttendees: List<AttendanceRecord>,
        progress: String?,
        newAttendance: Int?,
        total: Int?
    )
    suspend fun exportAttendanceCsv(eventId: String): Result<String>
    suspend fun clearCache()
}

