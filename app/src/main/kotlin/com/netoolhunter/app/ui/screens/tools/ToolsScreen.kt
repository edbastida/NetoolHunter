package com.netoolhunter.app.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.netoolhunter.app.domain.Category
import com.netoolhunter.app.ui.components.ConfirmDialog
import com.netoolhunter.app.ui.components.EmptyState
import com.netoolhunter.app.ui.components.ToolCard
import com.netoolhunter.app.ui.components.ToolCardState
import com.netoolhunter.app.ui.theme.BackgroundDark
import com.netoolhunter.app.ui.theme.KaliBlue
import com.netoolhunter.app.ui.theme.SurfaceVariant
import com.netoolhunter.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    initialCategoryId: String?,
    onNavigateToTerminal: () -> Unit,
    viewModel: ToolsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showUpdateConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(initialCategoryId) {
        viewModel.setInitialCategory(initialCategoryId)
    }

    LaunchedEffect(state.catalogUpdateMessage) {
        val msg = state.catalogUpdateMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.dismissCatalogMessage()
    }

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tools_title)) },
                actions = {
                    if (state.updatingCatalog) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(horizontal = 12.dp).size(20.dp),
                            color = KaliBlue,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { showUpdateConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.catalog_update_action),
                                tint = KaliBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text(stringResource(R.string.tools_search_hint)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant,
                    focusedIndicatorColor = KaliBlue,
                    unfocusedIndicatorColor = TextSecondary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                items(Category.entries.toList()) { cat ->
                    FilterChip(
                        selected = cat in state.selectedCategories,
                        onClick = { viewModel.toggleCategory(cat) },
                        label = { Text("${cat.emoji} ${cat.label}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KaliBlue.copy(alpha = 0.2f),
                            selectedLabelColor = KaliBlue
                        )
                    )
                }
            }

            val visible = state.visibleTools
            if (visible.isEmpty()) {
                EmptyState(emoji = "🔍", message = stringResource(R.string.tools_empty))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(visible, key = { it.id }) { tool ->
                        val isInstalled = tool.id in state.installedIds
                        ToolCard(
                            tool = tool,
                            state = if (isInstalled) ToolCardState.Installed else ToolCardState.Idle,
                            onPrimaryClick = {
                                viewModel.install(tool)
                                onNavigateToTerminal()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showUpdateConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.catalog_update_confirm_title),
            body = stringResource(R.string.catalog_update_confirm_body),
            onConfirm = {
                showUpdateConfirm = false
                viewModel.updateCatalog()
            },
            onDismiss = { showUpdateConfirm = false }
        )
    }
}
