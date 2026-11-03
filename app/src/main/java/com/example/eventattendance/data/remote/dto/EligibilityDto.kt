package com.example.eventattendance.data.remote.dto

import com.example.eventattendance.data.local.entity.EventEligibilityEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EligibilityDto(
    val id: String,
    @SerialName("event_id")
    val eventId: String,
    @SerialName("participant_id")
    val participantId: String
) {
    fun toEntity(): EventEligibilityEntity = EventEligibilityEntity(
        id = id,
        eventId = eventId,
        participantId = participantId
    )
}
