package com.example.eventattendance.data.remote.dto

import com.example.eventattendance.data.local.entity.EventEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("event_date")
    val eventDate: String? = null,
    @SerialName("start_time")
    val startTime: String? = null,
    @SerialName("end_time")
    val endTime: String? = null,
    val status: String = "open",
    @SerialName("created_at")
    val createdAt: String? = null
) {
    fun toEntity(): EventEntity = EventEntity(
        id = id,
        name = name,
        description = description,
        eventDate = eventDate,
        startTime = startTime,
        endTime = endTime,
        status = status,
        createdAt = createdAt
    )
}
