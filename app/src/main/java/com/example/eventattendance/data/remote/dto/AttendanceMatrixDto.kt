package com.example.eventattendance.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceMatrixResponseDto(
    val status: String? = null,
    @SerialName("total_participants")
    val totalParticipants: Int = 0,
    val events: List<MatrixEventDto> = emptyList()
)

@Serializable
data class MatrixEventDto(
    @SerialName("event_id")
    val eventId: Int,
    @SerialName("event_name")
    val eventName: String? = null,
    @SerialName("starts_from")
    val startsFrom: String? = null,
    @SerialName("ends_on")
    val endsOn: String? = null,
    @SerialName("present_p_ids")
    val presentPIds: List<String> = emptyList()
)
