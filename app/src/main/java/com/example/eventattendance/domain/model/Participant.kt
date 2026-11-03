package com.example.eventattendance.domain.model

data class Participant(
    val id: String,
    val usn: String,
    val name: String,
    val email: String? = null,
    val qrToken: String,
    val registrationStatus: String? = "confirmed",
    val checkedInAt: String? = null,
    val isCheckedIn: Boolean = false,
    val isPendingSync: Boolean = false
)
