package com.example.eventattendance.presentation.common

import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.presentation.theme.Gray200
import com.example.eventattendance.presentation.theme.MonochromeBlack
import com.example.eventattendance.presentation.theme.MonochromeWhite

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
            .background(MonochromeWhite)
            .border(1.dp, Gray200, CircleShape)
            .clickable { dial(context, phoneNumber) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Phone,
            contentDescription = "Call",
            tint = MonochromeBlack,
            modifier = Modifier.size(iconSize)
        )
    }
}
