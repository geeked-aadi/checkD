package com.example.eventattendance.presentation.eventdetails.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventattendance.domain.model.AttendanceRecord
import com.example.eventattendance.domain.model.CachedParticipant
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.presentation.common.CallButton
import com.example.eventattendance.presentation.theme.Gray100
import com.example.eventattendance.presentation.theme.Gray500
import com.example.eventattendance.presentation.theme.MonochromeBlack
import com.example.eventattendance.util.DateUtils

@Composable
fun ParticipantListItem(
    record: AttendanceRecord,
    currentRole: UserRole = UserRole.Volunteer,
    phoneNumber: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        // User Avatar Circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Gray100)
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MonochromeBlack,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Student Name & USN
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.participantName ?: "Participant",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            val subText = buildString {
                append(record.participantUsn ?: "")
                if (!record.teamId.isNullOrBlank()) {
                    append(" • Team: ${record.teamId}")
                }
            }

            Text(
                text = subText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = Gray500
            )
        }

        // CallButton (Only rendered if currentRole is Spoc or Admin)
        CallButton(
            phoneNumber = phoneNumber,
            currentRole = currentRole,
            size = 32.dp,
            iconSize = 16.dp,
            modifier = Modifier.padding(end = 8.dp)
        )

        // Timestamp
        Text(
            text = DateUtils.formatToTimeOnly(record.checkedInAt),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Gray500,
            modifier = Modifier.padding(end = 10.dp)
        )

        // Checkmark Circle Icon matching reference mockup
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Checked In",
            tint = MonochromeBlack,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun PendingParticipantListItem(
    participant: CachedParticipant,
    currentRole: UserRole,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Gray100)
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MonochromeBlack,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = participant.name ?: participant.pId,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            val subText = buildString {
                append(participant.pId)
                if (!participant.teamName.isNullOrBlank()) {
                    append(" • ${participant.teamName}")
                } else if (!participant.teamId.isNullOrBlank()) {
                    append(" • Team: ${participant.teamId}")
                }
            }

            Text(
                text = subText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = Gray500
            )
        }

        // CallButton next to pending participant (only for Spoc and Admin)
        CallButton(
            phoneNumber = participant.phonenumber,
            currentRole = currentRole
        )
    }
}
