package com.example.eventattendance.domain.model

data class Event(
    val id: String,
    val eventId: Int = id.toIntOrNull() ?: 0,
    val name: String,
    val startsFrom: String? = null,
    val endsOn: String? = null,
    val status: EventStatus = EventStatus.OPEN
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
