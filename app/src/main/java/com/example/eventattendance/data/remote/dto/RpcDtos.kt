package com.example.eventattendance.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAllParticipantsResponseDto(
    val status: String? = null,
    @SerialName("total_participants") val totalParticipants: Int? = null,
    val participants: List<ParticipantItemDto> = emptyList()
)

@Serializable
data class ParticipantItemDto(
    @SerialName("p_id") val pId: String,
    val name: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val phonenumber: String? = null,
    @SerialName("team_id") val teamId: String? = null,
    @SerialName("team_name") val teamName: String? = null,
    @SerialName("team_leader") val teamLeader: String? = null,
    @SerialName("spoc_id") val spocId: String? = null,
    @SerialName("spoc_name") val spocName: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    @SerialName("spoc_phonenumber") val spocPhonenumber: String? = null
)

@Serializable
data class SyncAllEventsAttendanceResponseDto(
    val status: String? = null,
    @SerialName("total_events") val totalEvents: Int? = null,
    @SerialName("total_participants") val totalParticipants: Int? = null,
    val events: List<EventAttendanceItemDto> = emptyList()
)

@Serializable
data class EventAttendanceItemDto(
    @SerialName("event_id") val eventId: Int,
    @SerialName("event_name") val eventName: String? = null,
    @SerialName("starts_from") val startsFrom: String? = null,
    @SerialName("ends_on") val endsOn: String? = null,
    val progress: String? = null,
    @SerialName("event_attendance") val eventAttendance: Int? = null,
    @SerialName("total_participants") val totalParticipants: Int? = null,
    @SerialName("present_p_ids") val presentPIds: List<String> = emptyList(),
    @SerialName("attendance_details") val attendanceDetails: List<AttendanceDetailDto> = emptyList()
)

@Serializable
data class AttendanceDetailDto(
    @SerialName("p_id") val pId: String,
    val name: String? = null,
    @SerialName("team_id") val teamId: String? = null,
    @SerialName("team_name") val teamName: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val phonenumber: String? = null,
    val time: String? = null
)

@Serializable
data class SyncEventAttendanceResponseDto(
    val status: String? = null,
    val message: String? = null,
    @SerialName("event_id") val eventId: Int? = null,
    @SerialName("event_name") val eventName: String? = null,
    val progress: String? = null,
    @SerialName("event_attendance") val eventAttendance: Int? = null,
    @SerialName("total_participants") val totalParticipants: Int? = null,
    @SerialName("present_p_ids") val presentPIds: List<String> = emptyList(),
    @SerialName("attendance_details") val attendanceDetails: List<AttendanceDetailDto> = emptyList()
)

@Serializable
data class MarkAttendanceResponseDto(
    val status: String? = null,
    val message: String? = null,
    @SerialName("marked_present") val markedPresent: List<String> = emptyList(),
    val duplicates: List<String> = emptyList(),
    val progress: String? = null,
    @SerialName("event_attendance") val eventAttendance: Int? = null,
    @SerialName("total_participants") val totalParticipants: Int? = null
)
