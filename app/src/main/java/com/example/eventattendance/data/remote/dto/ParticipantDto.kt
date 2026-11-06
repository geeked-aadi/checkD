package com.example.eventattendance.data.remote.dto

import com.example.eventattendance.domain.model.Participant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParticipantDto(
    @SerialName("p_id")
    val pId: String,
    val name: String? = null,
    @SerialName("team_id")
    val teamId: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val phonenumber: String? = null
) {
    fun toDomain(): Participant = Participant(
        id = pId,
        name = name ?: pId,
        teamId = teamId,
        phoneNumber = phonenumber,
        checkedInAt = null,
        isCheckedIn = false
    )
}
