package com.example.eventattendance.domain.model

data class AttendanceRecord(
    val id: String,
    val eventId: String,
    val participantId: String,
    val checkedInAt: String,
    val checkedInBy: String? = null,
    val status: String = "present",
    val participantName: String? = null,
    val participantUsn: String? = null,
    val isPendingSync: Boolean = false
)
