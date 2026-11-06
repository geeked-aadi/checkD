package com.example.eventattendance.domain.model

data class AttendanceMatrix(
    val status: String? = null,
    val totalParticipants: Int = 0,
    val events: List<EventAttendance> = emptyList()
)

data class EventAttendance(
    val eventId: Int,
    val eventName: String,
    val startsFrom: String? = null,
    val endsOn: String? = null,
    val presentPIds: Set<String> = emptySet()
)
