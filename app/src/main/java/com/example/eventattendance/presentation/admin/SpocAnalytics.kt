package com.example.eventattendance.presentation.admin

enum class SpocAnalyticsSort(val label: String) {
    LOWEST("Lowest"),
    HIGHEST("Highest"),
    NAME("Name")
}

data class SpocAnalytics(
    val spocId: String,
    val name: String,
    val phone: String?,
    val teamCount: Int,
    val studentCount: Int,
    val checked: Int,
    val total: Int,
    val pct: Float?, // null if 0 teams or 0 students/events; otherwise 0.0f..1.0f
    val pending: Int,
    val perEventPct: Map<Int, Int> = emptyMap() // eventId -> percentage (0..100)
)

data class SpocAnalyticsUiState(
    val spocs: List<SpocAnalytics> = emptyList(),
    val totalSpocs: Int = 0,
    val selectedEventId: Int? = null,
    val sortOption: SpocAnalyticsSort = SpocAnalyticsSort.LOWEST
)
