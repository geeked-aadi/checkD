package com.example.eventattendance.presentation.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.eventattendance.presentation.theme.Gray500
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val IST_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Asia/Kolkata"))

@Composable
fun LastUpdatedText(
    epochMillis: Long?,
    modifier: Modifier = Modifier
) {
    if (epochMillis == null) return

    val timeString = try {
        IST_TIME_FORMATTER.format(Instant.ofEpochMilli(epochMillis))
    } catch (_: Exception) {
        return
    }

    Text(
        text = "Updated $timeString",
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp
        ),
        color = Gray500,
        modifier = modifier
    )
}
