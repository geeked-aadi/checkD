package com.example.eventattendance.data.remote.dto

import com.example.eventattendance.domain.model.Spoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpocDto(
    @SerialName("spoc_id")
    val spocId: String,
    val name: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val phonenumber: String? = null
) {
    fun toDomain(): Spoc = Spoc(
        spocId = spocId,
        name = name ?: spocId,
        phoneNumber = phonenumber
    )
}
