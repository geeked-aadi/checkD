package com.example.eventattendance.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_scans",
    indices = [
        Index(value = ["eventId", "participantId"], unique = true),
        Index(value = ["syncStatus"])
    ]
)
data class PendingScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: String,
    val participantId: String,
    val qrToken: String,
    val scannedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING" // "PENDING", "SYNCED", "FAILED"
)
