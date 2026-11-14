package com.example.eventattendance.presentation.common

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.eventattendance.domain.model.ThemeMode
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.presentation.theme.CheckdTheme

fun dial(context: Context, number: String) {
    val clean = number.replace(Regex("[\\s-]"), "")
    if (clean.isNotBlank()) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
        context.startActivity(intent)
    }
}

@Composable
fun CallButton(
    phoneNumber: String?,
    currentRole: UserRole,
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    iconSize: Dp = 15.dp
) {
    // Only SPOC and Admin can see and use CallButton. Volunteers see no phone numbers or call buttons.
    if (currentRole !is UserRole.Spoc && currentRole !is UserRole.Admin) {
        return
    }

    if (phoneNumber.isNullOrBlank()) {
        return
    }

    val context = LocalContext.current

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable { dial(context, phoneNumber) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Phone,
            contentDescription = "Call",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Preview(name = "Call Button - Light")
@Composable
private fun CallButtonLightPreview() {
    CheckdTheme(themeMode = ThemeMode.LIGHT) {
        CallButton(phoneNumber = "1234567890", currentRole = UserRole.Admin)
    }
}

@Preview(name = "Call Button - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CallButtonDarkPreview() {
    CheckdTheme(themeMode = ThemeMode.DARK) {
        CallButton(phoneNumber = "1234567890", currentRole = UserRole.Admin)
    }
}

