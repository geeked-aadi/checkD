package com.example.eventattendance.presentation.scanner.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventattendance.domain.model.ScanResult
import com.example.eventattendance.presentation.theme.Gray500
import com.example.eventattendance.presentation.theme.MonochromeBlack
import com.example.eventattendance.presentation.theme.MonochromeCardSurface
import com.example.eventattendance.presentation.theme.StatusRed
import com.example.eventattendance.presentation.theme.StatusYellow

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

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MonochromeCardSurface),
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
                            tint = MonochromeBlack,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    is ScanResult.AlreadyCheckedIn -> {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Already Checked In",
                            tint = StatusYellow,
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
                            tint = StatusRed,
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
                                color = MonochromeBlack
                            )
                            Text(
                                text = scanResult.participantName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                color = MonochromeBlack
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
                                color = Gray500
                            )
                        }
                        is ScanResult.AlreadyCheckedIn -> {
                            Text(
                                text = "Already Checked In",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = StatusYellow
                            )
                            Text(
                                text = scanResult.participantName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MonochromeBlack
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
                                color = Gray500
                            )
                        }
                        is ScanResult.NetworkError -> {
                            Text(
                                text = "No Connection",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = StatusRed
                            )
                            Text(
                                text = "No connection, try again",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                        }
                        is ScanResult.NotEligible -> {
                            Text(
                                text = "Not Eligible for Activity",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = StatusRed
                            )
                            scanResult.participantName?.let {
                                Text(
                                    text = "$it (${scanResult.participantUsn})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray500
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
                                color = StatusRed
                            )
                            Text(
                                text = "Participant QR token not recognized",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                        }
                        is ScanResult.EventClosed -> {
                            Text(
                                text = "Event is Closed",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = StatusRed
                            )
                            Text(
                                text = "This activity is not accepting check-ins",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                        }
                        is ScanResult.Error -> {
                            Text(
                                text = "Scan Failed",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = StatusRed
                            )
                            Text(
                                text = scanResult.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                        }
                    }
                }
            }
        }
    }
}
