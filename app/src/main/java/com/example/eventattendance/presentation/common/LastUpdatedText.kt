package com.example.eventattendance.presentation.common

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.eventattendance.domain.model.ThemeMode
import com.example.eventattendance.presentation.theme.CheckdTheme
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
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Preview(name = "Last Updated - Light")
@Composable
private fun LastUpdatedTextLightPreview() {
    CheckdTheme(themeMode = ThemeMode.LIGHT) {
        LastUpdatedText(epochMillis = System.currentTimeMillis())
    }
}

@Preview(name = "Last Updated - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LastUpdatedTextDarkPreview() {
    CheckdTheme(themeMode = ThemeMode.DARK) {
        LastUpdatedText(epochMillis = System.currentTimeMillis())
    }
}

