package com.example.eventattendance.data.remote.dto

import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.domain.model.EventStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventDto(
    @SerialName("event_id")
    val eventId: Int,
    @SerialName("event_name")
    val eventName: String? = null,
    @SerialName("starts_from")
    val startsFrom: String? = null,
    @SerialName("ends_on")
    val endsOn: String? = null
) {
    fun toDomain(): Event = Event(
        id = eventId.toString(),
        eventId = eventId,
        name = eventName ?: "Event $eventId",
        startsFrom = startsFrom,
        endsOn = endsOn,
        status = EventStatus.OPEN
    )
}
