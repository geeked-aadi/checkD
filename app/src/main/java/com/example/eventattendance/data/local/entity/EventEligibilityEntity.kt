package com.example.eventattendance.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "event_eligibility",
    indices = [
        Index(value = ["eventId", "participantId"], unique = true),
        Index(value = ["eventId"]),
        Index(value = ["participantId"])
    ]
)
data class EventEligibilityEntity(
    @PrimaryKey
    val id: String,
    val eventId: String,
    val participantId: String
)
