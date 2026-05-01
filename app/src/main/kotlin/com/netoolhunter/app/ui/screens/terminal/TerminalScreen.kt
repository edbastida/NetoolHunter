package com.netoolhunter.app.ui.screens.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.netoolhunter.app.R
import com.netoolhunter.app.shell.TerminalBus
import com.netoolhunter.app.ui.components.TerminalView
import com.netoolhunter.app.ui.theme.BackgroundDark
import com.netoolhunter.app.ui.theme.BorderColor
import com.netoolhunter.app.ui.theme.ErrorColor
import com.netoolhunter.app.ui.theme.KaliBlue
import com.netoolhunter.app.ui.theme.SuccessColor
import com.netoolhunter.app.ui.theme.SurfaceDark
import com.netoolhunter.app.ui.theme.TerminalFont
import com.netoolhunter.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(viewModel: TerminalViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.terminal_title)) },
                actions = {
                    TextButton(onClick = { viewModel.clear() }) {
                        Text(stringResource(R.string.action_clear), color = KaliBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ActiveCommandChip(active = state.activeCommand)
            Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                TerminalView(lines = state.lines)
            }
        }
    }
}

@Composable
private fun ActiveCommandChip(active: TerminalBus.ActiveCommand?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .background(SurfaceDark, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (active == null) {
            Text(
                text = stringResource(R.string.state_idle),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        } else {
            val (label, color) = when (active.status) {
                TerminalBus.Status.RUNNING -> stringResource(R.string.state_running) to KaliBlue
                TerminalBus.Status.DONE    -> stringResource(R.string.state_done) to SuccessColor
                TerminalBus.Status.FAILED  -> stringResource(R.string.state_failed) to ErrorColor
            }
            Text(text = "● ", color = color, style = MaterialTheme.typography.bodySmall)
            Text(text = label, color = color, style = MaterialTheme.typography.bodySmall)
            Text(text = "  ·  ", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(
                text = active.command,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = TerminalFont),
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}
