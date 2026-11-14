package com.example.eventattendance.presentation.common

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventattendance.domain.model.ThemeMode
import com.example.eventattendance.presentation.theme.CheckdTheme

@Composable
fun AttendanceProgressBar(
    present: Int,
    total: Int,
    modifier: Modifier = Modifier,
    showCountText: Boolean = true
) {
    val progress = if (total > 0) (present.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    val percentage = (progress * 100).toInt()

    Column(modifier = modifier.fillMaxWidth()) {
        if (showCountText) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$present / $total",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        val trackShape = RoundedCornerShape(4.dp)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(trackShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, trackShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Preview(name = "Progress Bar - Light")
@Composable
private fun AttendanceProgressBarLightPreview() {
    CheckdTheme(themeMode = ThemeMode.LIGHT) {
        AttendanceProgressBar(present = 45, total = 100)
    }
}

@Preview(name = "Progress Bar - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttendanceProgressBarDarkPreview() {
    CheckdTheme(themeMode = ThemeMode.DARK) {
        AttendanceProgressBar(present = 45, total = 100)
    }
}

