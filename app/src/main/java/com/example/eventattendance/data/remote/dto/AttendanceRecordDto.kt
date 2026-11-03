package com.example.eventattendance.data.remote.dto

import com.example.eventattendance.data.local.entity.AttendanceRecordEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceRecordDto(
    val id: String,
    @SerialName("event_id")
    val eventId: String,
    @SerialName("participant_id")
    val participantId: String,
    @SerialName("checked_in_at")
    val checkedInAt: String,
    @SerialName("checked_in_by")
    val checkedInBy: String? = null,
    val status: String = "present"
) {
    fun toEntity(): AttendanceRecordEntity = AttendanceRecordEntity(
        id = id,
        eventId = eventId,
        participantId = participantId,
        checkedInAt = checkedInAt,
        checkedInBy = checkedInBy,
        status = status
    )
}
