package com.example.eventattendance.domain.model

data class EventWithStats(
    val event: Event,
    val scannedCount: Int,
    val totalEligibleCount: Int,
    val pendingSyncCount: Int = 0
) {
    val progress: Float
        get() = if (totalEligibleCount > 0) {
            (scannedCount.toFloat() / totalEligibleCount.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val percentage: Int
        get() = (progress * 100).toInt()
}
