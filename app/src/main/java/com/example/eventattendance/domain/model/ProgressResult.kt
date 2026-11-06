package com.example.eventattendance.domain.model

data class ProgressResult(
    val present: Int,
    val total: Int,
    val percent: Float
) {
    val displayFraction: String get() = "$present/$total"
    val percentageInt: Int get() = (percent * 100).toInt()
}

data class OverallResult(
    val percent: Float?,
    val presentSum: Int,
    val expectedSum: Int,
    val startedEventCount: Int
) {
    val displayPercent: String
        get() = if (percent != null) "${(percent * 100).toInt()}%" else "-"
}
