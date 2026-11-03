package com.example.eventattendance.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SingleAttendanceRpcResponse(
    val success: Boolean = false,
    @SerialName("already_checked_in")
    val alreadyCheckedIn: Boolean = false,
    val error: String? = null,
    @SerialName("participant_id")
    val participantId: String? = null,
    @SerialName("participant_name")
    val participantName: String? = null,
    @SerialName("participant_usn")
    val participantUsn: String? = null,
    @SerialName("checked_in_at")
    val checkedInAt: String? = null
)

@Serializable
data class BulkAttendanceRpcResponse(
    val success: Boolean = false,
    val inserted: Int = 0,
    val error: String? = null,
    @SerialName("event_id")
    val eventId: String? = null
)
