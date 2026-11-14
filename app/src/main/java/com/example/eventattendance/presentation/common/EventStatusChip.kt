package com.example.eventattendance.presentation.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventattendance.domain.model.ThemeMode
import com.example.eventattendance.domain.model.TimeBasedStatus
import com.example.eventattendance.presentation.theme.CheckdTheme
import com.example.eventattendance.presentation.theme.statusColors

@Composable
fun EventStatusChip(
    status: TimeBasedStatus,
    modifier: Modifier = Modifier
) {
    val statusColors = MaterialTheme.statusColors
    val (containerColor, textColor, hasBorder) = when (status) {
        TimeBasedStatus.LIVE -> Triple(statusColors.successContainer, statusColors.success, false)
        TimeBasedStatus.UPCOMING -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, true)
        TimeBasedStatus.ENDED -> Triple(MaterialTheme.colorScheme.inverseSurface, MaterialTheme.colorScheme.inverseOnSurface, true)
    }

    val chipShape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .clip(chipShape)
            .background(containerColor)
            .then(
                if (hasBorder) {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline, chipShape)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.3.sp
            ),
            color = textColor
        )
    }
}

@Preview(name = "Status Chip - Light")
@Composable
private fun EventStatusChipLightPreview() {
    CheckdTheme(themeMode = ThemeMode.LIGHT) {
        EventStatusChip(status = TimeBasedStatus.LIVE)
    }
}

@Preview(name = "Status Chip - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EventStatusChipDarkPreview() {
    CheckdTheme(themeMode = ThemeMode.DARK) {
        EventStatusChip(status = TimeBasedStatus.LIVE)
    }
}

