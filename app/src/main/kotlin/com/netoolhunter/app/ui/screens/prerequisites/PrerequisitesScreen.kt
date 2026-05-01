package com.netoolhunter.app.ui.screens.prerequisites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.netoolhunter.app.R
import com.netoolhunter.app.domain.RootManager
import com.netoolhunter.app.ui.components.ConfirmDialog
import com.netoolhunter.app.ui.theme.BackgroundDark
import com.netoolhunter.app.ui.theme.ErrorColor
import com.netoolhunter.app.ui.theme.KaliBlue
import com.netoolhunter.app.ui.theme.SuccessColor
import com.netoolhunter.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrerequisitesScreen(
    onCompleted: () -> Unit,
    viewModel: PrerequisitesViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.completed) {
        if (state.completed) onCompleted()
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.prereqs_title)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.prereqs_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            val status = state.status
            CheckRow(stringResource(R.string.prereqs_check_root), status?.rootAvailable == true)
            CheckRow(stringResource(R.string.prereqs_check_nh), status?.kaliEntrypoint == true)
            CheckRow(stringResource(R.string.prereqs_check_chroot), status?.chrootPresent == true)
            CheckRow(stringResource(R.string.prereqs_check_pkgs), status?.basePackages == true)

            if (status != null && !status.rootAvailable) {
                Text(
                    stringResource(R.string.prereqs_blocked_root),
                    color = ErrorColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (status != null && status.rootAvailable && status.chrootPresent && !status.kaliEntrypoint) {
                val hintRes = when (state.rootManager) {
                    RootManager.KernelSUNext -> R.string.prereqs_blocked_ksu_next
                    RootManager.KernelSU -> R.string.prereqs_blocked_ksu
                    RootManager.APatch -> R.string.prereqs_blocked_apatch
                    RootManager.MagiskOfficial -> R.string.prereqs_blocked_magisk
                    RootManager.Unknown -> R.string.prereqs_blocked_namespace
                }
                Text(
                    stringResource(hintRes),
                    color = ErrorColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (status != null && status.rootAvailable && status.chrootPresent && !status.basePackages) {
                Button(
                    onClick = { viewModel.showInstallConfirm() },
                    enabled = !state.installing,
                    colors = ButtonDefaults.buttonColors(containerColor = KaliBlue),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.prereqs_install_pkgs)) }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.recheck() },
                    enabled = !state.checking,
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.prereqs_recheck)) }

                Button(
                    onClick = { viewModel.markCompleted() },
                    enabled = status?.allOk == true,
                    colors = ButtonDefaults.buttonColors(containerColor = KaliBlue),
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.prereqs_continue)) }
            }
        }
    }

    if (state.showInstallConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.prereqs_install_pkgs_confirm_title),
            body = stringResource(R.string.prereqs_install_pkgs_confirm_body),
            onConfirm = { viewModel.installBasePackages() },
            onDismiss = { viewModel.hideInstallConfirm() }
        )
    }
}

@Composable
private fun CheckRow(label: String, ok: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (ok) "✓" else "✗",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (ok) SuccessColor else ErrorColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
