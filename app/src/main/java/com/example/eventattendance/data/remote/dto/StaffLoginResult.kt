package com.example.eventattendance.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StaffLoginRequest(
    val p_username: String,
    val p_password: String
)

@Serializable
data class StaffLoginResult(
    val ok: Boolean,
    val role: String? = null,
    @SerialName("spoc_id")
    val spocId: String? = null
)
