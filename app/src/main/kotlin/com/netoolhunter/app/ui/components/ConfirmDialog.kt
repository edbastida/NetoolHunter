package com.netoolhunter.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.netoolhunter.app.R
import com.netoolhunter.app.ui.theme.ErrorColor
import com.netoolhunter.app.ui.theme.KaliBlue

@Composable
fun ConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String = stringResource(R.string.action_confirm),
    cancelLabel: String = stringResource(R.string.action_cancel),
    destructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    color = if (destructive) ErrorColor else KaliBlue
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelLabel)
            }
        }
    )
}
