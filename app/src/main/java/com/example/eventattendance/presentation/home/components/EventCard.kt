package com.example.eventattendance.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.DinnerDining
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventattendance.domain.model.EventWithStats
import com.example.eventattendance.presentation.theme.Gray100
import com.example.eventattendance.presentation.theme.Gray500
import com.example.eventattendance.presentation.theme.MonochromeBlack
import com.example.eventattendance.presentation.theme.MonochromeCardBorder
import com.example.eventattendance.presentation.theme.MonochromeCardSurface

@Composable
fun EventCard(
    eventWithStats: EventWithStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val event = eventWithStats.event
    val icon = getEventIcon(event.name)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MonochromeCardSurface),
        border = BorderStroke(1.dp, MonochromeCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Icon + Event Name + Chevron
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Gray100,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MonochromeBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open Event Details",
                    tint = Gray500,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Stats Count: Bold Scanned / Expected Count
            val countText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    append("${eventWithStats.scannedCount}")
                }
                withStyle(
                    style = SpanStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = Gray500
                    )
                ) {
                    append(" / ${eventWithStats.totalEligibleCount}")
                }
            }

            Text(text = countText)

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar Row: Linear Indicator + Percentage
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = { eventWithStats.progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Gray100
                )

                Text(
                    text = "${eventWithStats.percentage}%",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = Gray500
                )
            }
        }
    }
}

private fun getEventIcon(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        lower.contains("breakfast") -> Icons.Outlined.Fastfood
        lower.contains("lunch") -> Icons.Outlined.Restaurant
        lower.contains("dinner") -> Icons.Outlined.DinnerDining
        lower.contains("workshop") || lower.contains("session") -> Icons.Outlined.School
        else -> Icons.Outlined.ConfirmationNumber
    }
}
