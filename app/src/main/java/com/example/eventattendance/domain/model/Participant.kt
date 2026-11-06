package com.example.eventattendance.domain.model

data class Participant(
    val id: String,
    val name: String,
    val teamId: String? = null,
    val phoneNumber: String? = null,
    val checkedInAt: String? = null,
    val isCheckedIn: Boolean = false,
    val isPendingSync: Boolean = false
) {
    val usn: String get() = id
    val qrToken: String get() = id
}
