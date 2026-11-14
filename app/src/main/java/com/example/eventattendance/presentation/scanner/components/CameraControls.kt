package com.example.eventattendance.presentation.scanner.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.eventattendance.presentation.theme.MonochromeWhite

@Composable
fun CameraControls(
    isTorchOn: Boolean,
    onTorchToggle: () -> Unit,
    onCameraSwitch: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MonochromeWhite,
                modifier = Modifier.size(24.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Flashlight Toggle
            IconButton(
                onClick = onTorchToggle,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                    contentDescription = "Toggle Flash",
                    tint = MonochromeWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Camera Switch (Front/Back)
            IconButton(
                onClick = onCameraSwitch,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = MonochromeWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
