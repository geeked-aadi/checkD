package com.example.eventattendance.domain.model

data class EventDetailData(
    val eventId: Int,
    val eventName: String,
    val startsFrom: String? = null,
    val endsOn: String? = null,
    val scannedCount: Int = 0,
    val totalExpected: Int = 0,
    val progress: String = "0/0",
    val attendees: List<AttendanceRecord> = emptyList()
) {
    val progressFraction: Float
        get() = if (totalExpected > 0) {
            (scannedCount.toFloat() / totalExpected.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val percentage: Int
        get() = (progressFraction * 100).toInt()
}
