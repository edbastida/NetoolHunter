package com.netoolhunter.app.ui.screens.repos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.netoolhunter.app.R
import com.netoolhunter.app.ui.components.ConfirmDialog
import com.netoolhunter.app.ui.components.RepoRow
import com.netoolhunter.app.ui.theme.BackgroundDark
import com.netoolhunter.app.ui.theme.KaliBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReposScreen(
    onNavigateToTerminal: () -> Unit,
    viewModel: ReposViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.repos_title)) },
                actions = {
                    TextButton(
                        onClick = { viewModel.showApplyConfirm() },
                        enabled = !state.applyInFlight
                    ) {
                        Text(stringResource(R.string.action_apply), color = KaliBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = KaliBlue
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_add_custom))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(state.repos, key = { it.id }) { repo ->
                RepoRow(
                    repo = repo,
                    onToggle = { _ -> viewModel.toggle(repo) },
                    onDelete = if (repo.isCustom) ({ viewModel.deleteCustom(repo) }) else null
                )
            }
        }
    }

    if (state.showAddDialog) {
        AddCustomRepoDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { name, line -> viewModel.addCustom(name, line) }
        )
    }

    if (state.showApplyConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.repos_apply_confirm_title),
            body = stringResource(R.string.repos_apply_confirm_body),
            destructive = false,
            onConfirm = {
                viewModel.applyChanges()
                onNavigateToTerminal()
            },
            onDismiss = { viewModel.hideApplyConfirm() }
        )
    }
}

@Composable
private fun AddCustomRepoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var line by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.repos_add_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.repos_field_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = line,
                    onValueChange = { line = it },
                    label = { Text(stringResource(R.string.repos_field_source_line)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank() && line.isNotBlank()) onConfirm(name.trim(), line.trim()) },
                enabled = name.isNotBlank() && line.isNotBlank()
            ) { Text(stringResource(R.string.action_confirm), color = KaliBlue) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
