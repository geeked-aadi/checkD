package com.example.eventattendance.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.eventattendance.domain.model.AttendanceRecord

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["eventId", "participantId"], unique = true),
        Index(value = ["eventId"])
    ]
)
data class AttendanceRecordEntity(
    @PrimaryKey
    val id: String,
    val eventId: String,
    val participantId: String,
    val checkedInAt: String,
    val checkedInBy: String?,
    val status: String
) {
    fun toDomain(participantName: String? = null, participantUsn: String? = null): AttendanceRecord =
        AttendanceRecord(
            id = id,
            eventId = eventId,
            participantId = participantId,
            checkedInAt = checkedInAt,
            checkedInBy = checkedInBy,
            status = status,
            participantName = participantName,
            participantUsn = participantUsn,
            isPendingSync = false
        )
}
