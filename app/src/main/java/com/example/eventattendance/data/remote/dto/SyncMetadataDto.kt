package com.example.eventattendance.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncMetadataDto(
    val key: String,
    @SerialName("last_modified_at")
    val lastModifiedAt: String
)
