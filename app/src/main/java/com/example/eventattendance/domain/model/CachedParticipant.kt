package com.example.eventattendance.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CachedParticipant(
    val pId: String,
    val name: String? = null,
    val phonenumber: String? = null,
    val teamId: String? = null,
    val teamName: String? = null,
    val teamLeader: String? = null,
    val spocId: String? = null,
    val spocName: String? = null,
    val spocPhonenumber: String? = null
) {
    fun toParticipant(): Participant = Participant(
        id = pId,
        name = name ?: pId,
        teamId = teamId,
        phoneNumber = phonenumber
    )
}
