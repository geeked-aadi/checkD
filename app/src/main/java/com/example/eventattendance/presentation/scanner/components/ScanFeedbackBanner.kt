package com.example.eventattendance.presentation.scanner.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventattendance.domain.model.ScanResult
import com.example.eventattendance.domain.model.ThemeMode
import com.example.eventattendance.presentation.theme.CheckdTheme
import com.example.eventattendance.presentation.theme.statusColors

@Composable
fun ScanFeedbackBanner(
    scanResult: ScanResult?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = scanResult != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        if (scanResult == null) return@AnimatedVisibility

        val statusColors = MaterialTheme.statusColors

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Status Icon
                when (scanResult) {
                    is ScanResult.Success -> {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = statusColors.success,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    is ScanResult.AlreadyCheckedIn -> {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Already Checked In",
                            tint = statusColors.warning,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    is ScanResult.NetworkError,
                    is ScanResult.NotEligible,
                    is ScanResult.InvalidQr,
                    is ScanResult.EventClosed,
                    is ScanResult.Error -> {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = "Error",
                            tint = statusColors.danger,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Feedback Content Column
                Column(modifier = Modifier.weight(1f)) {
                    when (scanResult) {
                        is ScanResult.Success -> {
                            Text(
                                text = if (scanResult.isPendingSync) "Checked In! (Offline)" else "Checked In!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = scanResult.participantName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val sub = buildString {
                                append(scanResult.participantUsn)
                                if (!scanResult.teamName.isNullOrBlank()) {
                                    append(" • ${scanResult.teamName}")
                                }
                            }
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is ScanResult.AlreadyCheckedIn -> {
                            Text(
                                text = "Already Checked In",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = statusColors.warning
                            )
                            Text(
                                text = scanResult.participantName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val sub = buildString {
                                append(scanResult.participantUsn)
                                if (!scanResult.teamName.isNullOrBlank()) {
                                    append(" • ${scanResult.teamName}")
                                }
                            }
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is ScanResult.NetworkError -> {
                            Text(
                                text = "No Connection",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = statusColors.danger
                            )
                            Text(
                                text = "No connection, try again",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is ScanResult.NotEligible -> {
                            Text(
                                text = "Not Eligible for Activity",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = statusColors.danger
                            )
                            scanResult.participantName?.let {
                                Text(
                                    text = "$it (${scanResult.participantUsn})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        is ScanResult.InvalidQr -> {
                            Text(
                                text = "Invalid QR Code",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = statusColors.danger
                            )
                            Text(
                                text = "Participant QR token not recognized",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is ScanResult.EventClosed -> {
                            Text(
                                text = "Event is Closed",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = statusColors.danger
                            )
                            Text(
                                text = "This activity is not accepting check-ins",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is ScanResult.Error -> {
                            Text(
                                text = "Scan Failed",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = statusColors.danger
                            )
                            Text(
                                text = scanResult.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Feedback Banner - Success Light")
@Composable
private fun ScanFeedbackBannerSuccessLightPreview() {
    CheckdTheme(themeMode = ThemeMode.LIGHT) {
        ScanFeedbackBanner(
            scanResult = ScanResult.Success(
                participantName = "John Doe",
                participantUsn = "1MS21CS001",
                teamName = "Alpha",
                isPendingSync = false
            )
        )
    }
}

@Preview(name = "Feedback Banner - Success Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ScanFeedbackBannerSuccessDarkPreview() {
    CheckdTheme(themeMode = ThemeMode.DARK) {
        ScanFeedbackBanner(
            scanResult = ScanResult.Success(
                participantName = "John Doe",
                participantUsn = "1MS21CS001",
                teamName = "Alpha",
                isPendingSync = false
            )
        )
    }
}

@Preview(name = "Feedback Banner - Warning Light")
@Composable
private fun ScanFeedbackBannerWarningLightPreview() {
    CheckdTheme(themeMode = ThemeMode.LIGHT) {
        ScanFeedbackBanner(
            scanResult = ScanResult.AlreadyCheckedIn(
                participantName = "John Doe",
                participantUsn = "1MS21CS001",
                teamName = "Alpha"
            )
        )
    }
}

@Preview(name = "Feedback Banner - Warning Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ScanFeedbackBannerWarningDarkPreview() {
    CheckdTheme(themeMode = ThemeMode.DARK) {
        ScanFeedbackBanner(
            scanResult = ScanResult.AlreadyCheckedIn(
                participantName = "John Doe",
                participantUsn = "1MS21CS001",
                teamName = "Alpha"
            )
        )
    }
}

