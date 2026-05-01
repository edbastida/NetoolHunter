package com.netoolhunter.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.netoolhunter.app.domain.InstallEvent
import com.netoolhunter.app.ui.theme.TerminalBg
import com.netoolhunter.app.ui.theme.TerminalGreen
import com.netoolhunter.app.ui.theme.TerminalRed
import com.netoolhunter.app.ui.theme.TerminalTextStyle
import com.netoolhunter.app.ui.theme.TerminalYellow

data class TerminalLine(val text: String, val color: Color)

fun InstallEvent.toLine(): TerminalLine = when (this) {
    is InstallEvent.Started -> TerminalLine("$ $command", TerminalYellow)
    is InstallEvent.Stdout  -> TerminalLine(line, TerminalGreen)
    is InstallEvent.Stderr  -> TerminalLine(line, TerminalRed)
    is InstallEvent.Exit    -> TerminalLine("[exit code=$code]", if (code == 0) TerminalYellow else TerminalRed)
    is InstallEvent.Error   -> TerminalLine("[error: $message]", TerminalRed)
}

@Composable
fun TerminalView(
    lines: List<TerminalLine>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.lastIndex)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(8.dp)
    ) {
        if (lines.isEmpty()) {
            Column {
                Text(
                    text = "$ _",
                    style = TerminalTextStyle,
                    color = TerminalGreen
                )
            }
        } else {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(lines) { line ->
                    Text(
                        text = line.text,
                        style = TerminalTextStyle,
                        color = line.color
                    )
                }
            }
        }
    }
}
