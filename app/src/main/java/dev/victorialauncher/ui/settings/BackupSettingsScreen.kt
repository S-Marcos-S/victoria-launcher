// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.victorialauncher.R
import dev.victorialauncher.backup.BackupFrequency
import dev.victorialauncher.backup.BackupItem
import dev.victorialauncher.backup.BackupManager
import dev.victorialauncher.backup.BackupType
import dev.victorialauncher.data.Prefs
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BackupSettingsScreen(
    prefs: Prefs,
    autoBackupEnabled: Boolean,
    autoBackupFrequency: BackupFrequency,
    backupMaxKeep: Int,
    backupIncludeWallpaper: Boolean,
    lastAutoBackupTimestamp: Long,
    onSetAutoBackupEnabled: (Boolean) -> Unit,
    onSetAutoBackupFrequency: (BackupFrequency) -> Unit,
    onSetBackupMaxKeep: (Int) -> Unit,
    onSetBackupIncludeWallpaper: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    var backupList by remember { mutableStateOf(BackupManager.listBackups(context)) }
    var isCreating by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }

    var backupToRestore by remember { mutableStateOf<BackupItem?>(null) }
    var externalUriToRestore by remember { mutableStateOf<Uri?>(null) }
    var backupToDelete by remember { mutableStateOf<BackupItem?>(null) }

    fun refreshList() {
        backupList = BackupManager.listBackups(context)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            externalUriToRestore = uri
        }
    }

    Scaffold(
        containerColor = colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_backup_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surface),
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            // Card Principal: Criar e Restaurar
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.Backup,
                                    contentDescription = null,
                                    tint = colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                            Column {
                                Text(
                                    stringResource(R.string.backup_create_card_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    stringResource(R.string.backup_create_card_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colorScheme.primary,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = !isCreating) {
                                        isCreating = true
                                        scope.launch {
                                            val res = BackupManager.createBackup(
                                                context = context,
                                                prefs = prefs,
                                                type = BackupType.MANUAL,
                                                includeWallpaper = backupIncludeWallpaper,
                                                maxKeep = backupMaxKeep,
                                            )
                                            isCreating = false
                                            if (res.isSuccess) {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.backup_created_success),
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                                refreshList()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.backup_create_failed),
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                        }
                                    },
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (isCreating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = colorScheme.onPrimary,
                                            strokeWidth = 2.dp,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            stringResource(R.string.backup_creating),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    } else {
                                        Icon(
                                            Icons.Filled.Backup,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = colorScheme.onPrimary,
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            stringResource(R.string.backup_create_now),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colorScheme.surfaceContainerHighest,
                                border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        filePickerLauncher.launch("*/*")
                                    },
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Filled.FileOpen,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = colorScheme.onSurface,
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        stringResource(R.string.backup_restore_from_file),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Seção: Backups Automáticos
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.backup_auto_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    stringResource(R.string.backup_auto_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = autoBackupEnabled,
                                onCheckedChange = onSetAutoBackupEnabled,
                                colors = SwitchDefaults.colors(checkedThumbColor = colorScheme.primary),
                            )
                        }

                        AnimatedVisibility(visible = autoBackupEnabled) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                // Seletor de Frequência
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        stringResource(R.string.backup_frequency),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colorScheme.onSurfaceVariant,
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        BackupFrequency.entries.forEach { freq ->
                                            val selected = freq == autoBackupFrequency
                                            FilterChip(
                                                selected = selected,
                                                onClick = { onSetAutoBackupFrequency(freq) },
                                                label = { Text(stringResource(freq.labelRes)) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = colorScheme.primaryContainer,
                                                    selectedLabelColor = colorScheme.onPrimaryContainer,
                                                ),
                                            )
                                        }
                                    }
                                }

                                // Seletor de Retenção (Quantos backups manter)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        stringResource(R.string.backup_max_keep),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        stringResource(R.string.backup_max_keep_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        listOf(3, 5, 10, 20).forEach { count ->
                                            val selected = count == backupMaxKeep
                                            FilterChip(
                                                selected = selected,
                                                onClick = { onSetBackupMaxKeep(count) },
                                                label = { Text("$count backups") },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = colorScheme.primaryContainer,
                                                    selectedLabelColor = colorScheme.onPrimaryContainer,
                                                ),
                                            )
                                        }
                                    }
                                }

                                // Informação do último backup automático
                                val lastDateFormatted = remember(lastAutoBackupTimestamp) {
                                    if (lastAutoBackupTimestamp > 0L) {
                                        val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                        fmt.format(Date(lastAutoBackupTimestamp))
                                    } else null
                                }
                                Text(
                                    text = if (lastDateFormatted != null) {
                                        stringResource(R.string.backup_last_auto, lastDateFormatted)
                                    } else {
                                        stringResource(R.string.backup_last_auto_none)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }

            // Seção: Opções de Conteúdo (Papel de parede)
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.backup_include_wallpaper),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                stringResource(R.string.backup_include_wallpaper_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = backupIncludeWallpaper,
                            onCheckedChange = onSetBackupIncludeWallpaper,
                            colors = SwitchDefaults.colors(checkedThumbColor = colorScheme.primary),
                        )
                    }
                }
            }

            // Título da Lista de Backups Salvos
            item {
                Text(
                    stringResource(R.string.backup_saved_backups, backupList.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }

            // Lista de Backups Salvos ou Estado Vazio
            if (backupList.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Filled.Backup,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            )
                            Text(
                                stringResource(R.string.backup_no_backups),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface,
                            )
                            Text(
                                stringResource(R.string.backup_no_backups_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(backupList, key = { it.file.absolutePath }) { item ->
                    BackupCardItem(
                        item = item,
                        onRestore = { backupToRestore = item },
                        onShare = { BackupManager.shareBackup(context, item.file) },
                        onDelete = { backupToDelete = item },
                    )
                }
            }
        }
    }

    // Diálogo de Confirmação de Restauração Local
    backupToRestore?.let { item ->
        AlertDialog(
            onDismissRequest = { backupToRestore = null },
            title = { Text(stringResource(R.string.backup_confirm_restore_title)) },
            text = { Text(stringResource(R.string.backup_confirm_restore_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toRestore = item
                        backupToRestore = null
                        isRestoring = true
                        scope.launch {
                            val res = BackupManager.restoreBackup(context, prefs, toRestore.file)
                            isRestoring = false
                            if (res.isSuccess) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.backup_restore_success),
                                    Toast.LENGTH_LONG,
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.backup_restore_failed),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    },
                ) {
                    Text(
                        stringResource(R.string.backup_action_restore),
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { backupToRestore = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    // Diálogo de Confirmação de Restauração Externa (URI)
    externalUriToRestore?.let { uri ->
        AlertDialog(
            onDismissRequest = { externalUriToRestore = null },
            title = { Text(stringResource(R.string.backup_confirm_restore_title)) },
            text = { Text(stringResource(R.string.backup_confirm_restore_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        externalUriToRestore = null
                        isRestoring = true
                        scope.launch {
                            val res = BackupManager.restoreBackupFromUri(context, prefs, uri)
                            isRestoring = false
                            if (res.isSuccess) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.backup_restore_success),
                                    Toast.LENGTH_LONG,
                                ).show()
                                refreshList()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.backup_restore_failed),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    },
                ) {
                    Text(
                        stringResource(R.string.backup_action_restore),
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { externalUriToRestore = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    // Diálogo de Confirmação de Exclusão
    backupToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { backupToDelete = null },
            title = { Text(stringResource(R.string.backup_confirm_delete_title)) },
            text = { Text(stringResource(R.string.backup_confirm_delete_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDel = item
                        backupToDelete = null
                        BackupManager.deleteBackup(toDel.file)
                        Toast.makeText(
                            context,
                            context.getString(R.string.backup_deleted_success),
                            Toast.LENGTH_SHORT,
                        ).show()
                        refreshList()
                    },
                ) {
                    Text(
                        stringResource(R.string.backup_action_delete),
                        color = colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { backupToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun BackupCardItem(
    item: BackupItem,
    onRestore: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val meta = item.meta
    val isAuto = meta?.type == BackupType.AUTOMATIC

    val displayDate = remember(item.file) {
        meta?.formattedDate?.ifBlank { null } ?: run {
            val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            fmt.format(Date(item.file.lastModified()))
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )

                // Badge Tipo (Manual ou Automático)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAuto) colorScheme.secondaryContainer else colorScheme.primaryContainer,
                ) {
                    Text(
                        text = stringResource(if (isAuto) R.string.backup_tag_auto else R.string.backup_tag_manual),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAuto) colorScheme.onSecondaryContainer else colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = item.formattedSize,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )

                if (meta?.hasWallpaper == true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Image,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = colorScheme.primary,
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = stringResource(R.string.backup_has_wallpaper),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.primary,
                            fontSize = 11.sp,
                        )
                    }
                }
            }

            // Ações: Restaurar, Compartilhar, Excluir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onShare) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.backup_action_share),
                        fontSize = 12.sp,
                    )
                }

                TextButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = colorScheme.error,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.backup_action_delete),
                        color = colorScheme.error,
                        fontSize = 12.sp,
                    )
                }

                Spacer(Modifier.width(4.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onRestore),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Restore,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.backup_action_restore),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
}
