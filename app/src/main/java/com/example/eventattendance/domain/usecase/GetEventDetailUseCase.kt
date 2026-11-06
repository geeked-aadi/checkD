package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.model.AttendanceRecord
import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.domain.model.EventDetailData
import com.example.eventattendance.domain.repository.AttendanceRepository
import com.example.eventattendance.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetEventDetailUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val attendanceRepository: AttendanceRepository
) {
    fun getEvent(eventId: String): Flow<Event?> {
        return eventRepository.getEventByIdFlow(eventId)
    }

    fun getEventDetail(eventId: Int): Flow<EventDetailData?> {
        return attendanceRepository.getEventAttendanceFlow(eventId)
    }

    fun getAttendanceList(eventId: String): Flow<List<AttendanceRecord>> {
        val id = eventId.toIntOrNull() ?: 0
        return attendanceRepository.getEventAttendanceFlow(id).map { it?.attendees ?: emptyList() }
    }

    fun getScannedCount(eventId: String): Flow<Int> {
        val id = eventId.toIntOrNull() ?: 0
        return attendanceRepository.getEventAttendanceFlow(id).map { it?.scannedCount ?: 0 }
    }
}
