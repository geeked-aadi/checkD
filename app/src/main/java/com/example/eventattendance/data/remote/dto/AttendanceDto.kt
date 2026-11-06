package com.example.eventattendance.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceDto(
    @SerialName("p_id")
    val pId: String,
    @SerialName("event_id")
    val eventId: Int,
    val time: String? = null
)
