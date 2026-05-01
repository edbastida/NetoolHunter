package com.netoolhunter.app.ui.screens.installed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.netoolhunter.app.ui.components.ConfirmDialog
import com.netoolhunter.app.ui.components.EmptyState
import com.netoolhunter.app.ui.components.ToolCard
import com.netoolhunter.app.ui.components.ToolCardState
import com.netoolhunter.app.ui.theme.BackgroundDark
import com.netoolhunter.app.ui.theme.KaliBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledScreen(
    onNavigateToTerminal: () -> Unit,
    viewModel: InstalledViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.installed_title)) },
                actions = {
                    TextButton(onClick = { viewModel.rescan() }) {
                        Text(stringResource(R.string.prereqs_recheck), color = KaliBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.scanning && state.installedTools.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp), color = KaliBlue)
                    }
                }
                state.installedTools.isEmpty() -> {
                    EmptyState(emoji = "📭", message = stringResource(R.string.installed_empty))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(state.installedTools, key = { it.id }) { tool ->
                            ToolCard(
                                tool = tool,
                                state = ToolCardState.Installed,
                                showUninstall = true,
                                onPrimaryClick = { viewModel.requestUninstall(tool) },
                                onUninstallClick = { viewModel.requestUninstall(tool) }
                            )
                        }
                    }
                }
            }
        }
    }

    state.pendingUninstall?.let { tool ->
        ConfirmDialog(
            title = stringResource(R.string.installed_uninstall_confirm_title, tool.name),
            body = stringResource(R.string.installed_uninstall_confirm_body, tool.name),
            destructive = true,
            onConfirm = {
                viewModel.confirmUninstall()
                onNavigateToTerminal()
            },
            onDismiss = { viewModel.cancelUninstall() }
        )
    }
}
