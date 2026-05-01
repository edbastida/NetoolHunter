package com.netoolhunter.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.netoolhunter.app.domain.Repo
import com.netoolhunter.app.ui.theme.BorderColor
import com.netoolhunter.app.ui.theme.ErrorColor
import com.netoolhunter.app.ui.theme.KaliBlue
import com.netoolhunter.app.ui.theme.SurfaceDark
import com.netoolhunter.app.ui.theme.TerminalFont
import com.netoolhunter.app.ui.theme.TextSecondary

@Composable
fun RepoRow(
    repo: Repo,
    onToggle: (Boolean) -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = repo.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (repo.isCustom) {
                        Text(
                            text = "custom",
                            style = MaterialTheme.typography.labelSmall,
                            color = KaliBlue
                        )
                    }
                }
                Text(
                    text = repo.sourceLine,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = TerminalFont),
                    color = TextSecondary,
                    maxLines = 2
                )
            }
            if (repo.isCustom && onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = ErrorColor)
                }
            }
            Switch(
                checked = repo.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = KaliBlue,
                    checkedTrackColor = KaliBlue.copy(alpha = 0.4f)
                )
            )
        }
    }
}
