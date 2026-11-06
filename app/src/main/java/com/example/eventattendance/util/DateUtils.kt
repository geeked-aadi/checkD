package com.example.eventattendance.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    fun formatToTimeOnly(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) return "Just now"

        return try {
            val normalized = isoTimestamp.replace(' ', 'T').substringBefore('.')
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date: Date = inputFormat.parse(normalized) ?: return isoTimestamp

            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
            outputFormat.format(date)
        } catch (e: Exception) {
            isoTimestamp
        }
    }

    fun formatTimeWindow(startsFrom: String?, endsOn: String?): String {
        val startStr = if (!startsFrom.isNullOrBlank()) formatToTimeOnly(startsFrom) else null
        val endStr = if (!endsOn.isNullOrBlank()) formatToTimeOnly(endsOn) else null

        return when {
            startStr != null && endStr != null -> "$startStr – $endStr"
            startStr != null -> "Starts $startStr"
            endStr != null -> "Until $endStr"
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
