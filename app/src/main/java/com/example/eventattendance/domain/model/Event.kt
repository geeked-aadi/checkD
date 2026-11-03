package com.example.eventattendance.domain.model

data class Event(
    val id: String,
    val name: String,
    val description: String? = null,
    val eventDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val status: EventStatus = EventStatus.OPEN,
    val createdAt: String? = null
)

enum class EventStatus(val value: String) {
    OPEN("open"),
    CLOSED("closed");

    companion object {
        fun fromString(value: String): EventStatus {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: OPEN
        }
    }
}
