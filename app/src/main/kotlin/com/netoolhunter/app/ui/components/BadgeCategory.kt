package com.netoolhunter.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.netoolhunter.app.domain.Category

@Composable
fun BadgeCategory(category: Category, modifier: Modifier = Modifier) {
    Text(
        text = "${category.emoji} ${category.label}",
        style = MaterialTheme.typography.labelSmall,
        color = category.accent,
        modifier = modifier
            .border(1.dp, category.accent.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .background(category.accent.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun BadgeInstaller(kind: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = kind.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
