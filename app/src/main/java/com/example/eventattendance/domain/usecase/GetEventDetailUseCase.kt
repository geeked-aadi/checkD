package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.model.AttendanceRecord
import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.domain.repository.AttendanceRepository
import com.example.eventattendance.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventDetailUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val attendanceRepository: AttendanceRepository
) {
    fun getEvent(eventId: String): Flow<Event?> {
        return eventRepository.getEventByIdFlow(eventId)
    }

    fun getAttendanceList(eventId: String): Flow<List<AttendanceRecord>> {
        return attendanceRepository.getAttendanceForEventFlow(eventId)
    }

    fun getScannedCount(eventId: String): Flow<Int> {
        return attendanceRepository.getScannedCountFlow(eventId)
    }
}
