package com.example.eventattendance.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    fun formatToTimeOnly(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) return "Just now"

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date: Date = inputFormat.parse(isoTimestamp.substringBefore('.')) ?: return isoTimestamp

            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
            outputFormat.format(date)
        } catch (e: Exception) {
            isoTimestamp
        }
    }
}
