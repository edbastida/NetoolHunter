package com.netoolhunter.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netoolhunter.app.R
import com.netoolhunter.app.domain.Tool
import com.netoolhunter.app.ui.theme.BorderColor
import com.netoolhunter.app.ui.theme.ErrorColor
import com.netoolhunter.app.ui.theme.KaliBlue
import com.netoolhunter.app.ui.theme.SuccessColor
import com.netoolhunter.app.ui.theme.SurfaceDark
import com.netoolhunter.app.ui.theme.TextSecondary

enum class ToolCardState { Idle, Installing, Installed }

@Composable
fun ToolCard(
    tool: Tool,
    state: ToolCardState,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    showUninstall: Boolean = false,
    onUninstallClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = tool.emoji, fontSize = 32.sp)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BadgeCategory(tool.category)
                    BadgeInstaller(tool.installer.kindLabel, KaliBlue)
                }
            }

            when (state) {
                ToolCardState.Idle -> {
                    if (showUninstall) {
                        OutlinedButton(
                            onClick = onPrimaryClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor)
                        ) {
                            Text(stringResourceCompat(R.string.action_uninstall))
                        }
                    } else {
                        Button(
                            onClick = onPrimaryClick,
                            colors = ButtonDefaults.buttonColors(containerColor = KaliBlue)
                        ) {
                            Text(stringResourceCompat(R.string.action_install))
                        }
                    }
                }
                ToolCardState.Installing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = KaliBlue
                    )
                }
                ToolCardState.Installed -> {
                    if (showUninstall) {
                        OutlinedButton(
                            onClick = onUninstallClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor)
                        ) {
                            Text(stringResourceCompat(R.string.action_uninstall))
                        }
                    } else {
                        Text(
                            text = "✓ ${stringResourceCompat(R.string.state_installed)}",
                            color = SuccessColor,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun stringResourceCompat(resId: Int): String =
    androidx.compose.ui.res.stringResource(id = resId)
