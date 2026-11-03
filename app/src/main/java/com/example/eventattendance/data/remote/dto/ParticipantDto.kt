package com.example.eventattendance.data.remote.dto

import com.example.eventattendance.data.local.entity.ParticipantEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParticipantDto(
    val id: String,
    val usn: String,
    val name: String,
    val email: String? = null,
    @SerialName("qr_token")
    val qrToken: String,
    @SerialName("registration_status")
    val registrationStatus: String? = "confirmed",
    @SerialName("created_at")
    val createdAt: String? = null
) {
    fun toEntity(): ParticipantEntity = ParticipantEntity(
        id = id,
        usn = usn,
        name = name,
        email = email,
        qrToken = qrToken,
        registrationStatus = registrationStatus
    )
}
