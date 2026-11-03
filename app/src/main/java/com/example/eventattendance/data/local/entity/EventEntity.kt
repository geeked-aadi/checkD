package com.example.eventattendance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.domain.model.EventStatus

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val eventDate: String?,
    val startTime: String?,
    val endTime: String?,
    val status: String,
    val createdAt: String?
) {
    fun toDomain(): Event = Event(
        id = id,
        name = name,
        description = description,
        eventDate = eventDate,
        startTime = startTime,
        endTime = endTime,
        status = EventStatus.fromString(status),
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(event: Event): EventEntity = EventEntity(
            id = event.id,
            name = event.name,
            description = event.description,
            eventDate = event.eventDate,
            startTime = event.startTime,
            endTime = event.endTime,
            status = event.status.value,
            createdAt = event.createdAt
        )
    }
}
