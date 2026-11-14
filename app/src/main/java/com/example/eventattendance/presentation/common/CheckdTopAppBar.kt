package com.example.eventattendance.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventattendance.R

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.example.eventattendance.domain.model.ThemeMode
import com.example.eventattendance.presentation.theme.CheckdTheme

@Composable
fun CheckdHomeTopBar(
    onVerifierClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Logo Icon matching reference
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(42.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_checkd_logo),
                contentDescription = "checkD Logo",
                modifier = Modifier
                    .size(42.dp)
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "checkD",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.weight(1f))

        // QR Verifier Button
        IconButton(
            onClick = onVerifierClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.QrCodeScanner,
                contentDescription = "QR Verifier",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview(name = "Home Top Bar - Light")
@Composable
private fun CheckdHomeTopBarLightPreview() {
    CheckdTheme(themeMode = ThemeMode.LIGHT) {
        CheckdHomeTopBar(onVerifierClick = {}, onSettingsClick = {})
    }
}

@Preview(name = "Home Top Bar - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CheckdHomeTopBarDarkPreview() {
    CheckdTheme(themeMode = ThemeMode.DARK) {
        CheckdHomeTopBar(onVerifierClick = {}, onSettingsClick = {})
    }
}


