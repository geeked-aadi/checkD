package com.example.eventattendance.util

import com.example.eventattendance.domain.calculator.ProgressCalculator
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

    fun formatToTimeOnly(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) return "Just now"

        return try {
            val localDateTime = ProgressCalculator.parseToIstLocalDateTime(isoTimestamp) ?: return isoTimestamp
            localDateTime.format(TIME_FORMATTER)
        } catch (_: Exception) {
            isoTimestamp
        }
    }

    fun formatTimeWindow(startsFrom: String?, endsOn: String?): String {
        val start = ProgressCalculator.parseToIstLocalDateTime(startsFrom)
        val end = ProgressCalculator.parseToIstLocalDateTime(endsOn)

        if (start == null && end == null) return ""

        val startText = start?.let { it.format(DATE_FORMATTER) to it.format(TIME_FORMATTER) }
        val endText = end?.let { it.format(DATE_FORMATTER) to it.format(TIME_FORMATTER) }

        return when {
            startText != null && endText != null -> {
                val sameDay = start.toLocalDate() == end.toLocalDate()
                if (sameDay) {
                    "${startText.first}, ${startText.second} – ${endText.second}"
                } else {
                    "${startText.first}, ${startText.second} – ${endText.first}, ${endText.second}"
                }
            }
            startText != null -> "Starts ${startText.first}, ${startText.second}"
            endText != null -> "Until ${endText.first}, ${endText.second}"
            else -> ""
        }
    }

    fun getCurrentIsoTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date())
    }
}
