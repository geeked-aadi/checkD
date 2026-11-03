package com.example.eventattendance.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.eventattendance.domain.model.Participant

@Entity(
    tableName = "participants",
    indices = [
        Index(value = ["qrToken"], unique = true),
        Index(value = ["usn"], unique = true)
    ]
)
data class ParticipantEntity(
    @PrimaryKey
    val id: String,
    val usn: String,
    val name: String,
    val email: String?,
    val qrToken: String,
    val registrationStatus: String?
) {
    fun toDomain(): Participant = Participant(
        id = id,
        usn = usn,
        name = name,
        email = email,
        qrToken = qrToken,
        registrationStatus = registrationStatus
    )
}
