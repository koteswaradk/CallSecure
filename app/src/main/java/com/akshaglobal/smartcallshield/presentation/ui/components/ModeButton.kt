package com.akshaglobal.smartcallshield.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ModeButton(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val iconTint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(80.dp)
            .padding(4.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        border = BorderStroke(1.5.dp, borderColor),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = if (enabled) iconTint else iconTint.copy(alpha = 0.38f)
        )
    }
}

