package com.example.toodo.ui.screen.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

// ─── Option presets ────────────────────────────────────────────────────────────

private val THEME_OPTIONS = listOf(
    "system" to "跟随系统",
    "light"  to "浅色",
    "dark"   to "深色"
)

private val REMINDER_OPTIONS = listOf(
    10080L to "前1周",
    1440L  to "前1天",
    60L    to "前1小时"
)

private val WEEK_OPTIONS = listOf(
    1 to "周一",
    7 to "周日"
)

// ═════════════════════════════════════════════════════════════════════════════
//  SettingsScreen
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onManageLists: () -> Unit = {}
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Dialog visibility ────────────────────────────────────────────────────
    var showThemeDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // ── SAF launchers ────────────────────────────────────────────────────────
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val json = viewModel.buildExportJson()
                    context.contentResolver.openOutputStream(it)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                    snackbarHostState.showSnackbar("数据导出成功")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("导出失败: ${e.message}")
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val json = context.contentResolver.openInputStream(it)?.use { input ->
                        input.readBytes().toString(Charsets.UTF_8)
                    } ?: throw Exception("无法读取文件")
                    viewModel.importFromJson(json)
                    snackbarHostState.showSnackbar("数据导入成功")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("导入失败: ${e.message}")
                }
            }
        }
    }

    // ── Theme dialog ─────────────────────────────────────────────────────────
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("选择主题") },
            text = {
                Column(Modifier.selectableGroup()) {
                    THEME_OPTIONS.forEach { (value, label) ->
                        val selected = uiState.darkMode == value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = selected, onClick = {
                                    viewModel.setDarkMode(value)
                                    showThemeDialog = false
                                }, role = Role.RadioButton)
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selected, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("取消") } }
        )
    }

    // ── Time picker dialog ───────────────────────────────────────────────────
    if (showTimePicker) {
        var pickerHour by remember { mutableIntStateOf(uiState.dailyResetHour) }
        var pickerMinute by remember { mutableIntStateOf(uiState.dailyResetMinute) }
        LaunchedEffect(showTimePicker) {
            pickerHour = uiState.dailyResetHour
            pickerMinute = uiState.dailyResetMinute
        }
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("每日重置时间") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { pickerHour = (pickerHour + 1) % 24 }) {
                            Text("▲", style = MaterialTheme.typography.titleMedium)
                        }
                        Text(String.format("%02d", pickerHour), style = MaterialTheme.typography.headlineMedium)
                        IconButton(onClick = { pickerHour = (pickerHour - 1 + 24) % 24 }) {
                            Text("▼", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 8.dp))
                    // Minute (5-min steps)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { pickerMinute = (pickerMinute + 5) % 60 }) {
                            Text("▲", style = MaterialTheme.typography.titleMedium)
                        }
                        Text(String.format("%02d", pickerMinute), style = MaterialTheme.typography.headlineMedium)
                        IconButton(onClick = { pickerMinute = (pickerMinute - 5 + 60) % 60 }) {
                            Text("▼", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setDailyResetTime(pickerHour, pickerMinute)
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } }
        )
    }

    // ── Main scaffold ────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // 1. 主题
            item {
                SettingsDropdownItem(
                    label = "主题",
                    currentValue = THEME_OPTIONS.firstOrNull { it.first == uiState.darkMode }?.second ?: "跟随系统",
                    onClick = { showThemeDialog = true }
                )
            }
            // 2. 每日重置时间
            item {
                SettingsClickableItem(
                    label = "每日重置时间",
                    value = String.format("%02d:%02d", uiState.dailyResetHour, uiState.dailyResetMinute),
                    onClick = { showTimePicker = true }
                )
            }
            // 3. 默认提醒
            item {
                SettingsChipGroupItem(
                    label = "默认提醒",
                    options = REMINDER_OPTIONS,
                    selectedValues = uiState.defaultReminderOffsets,
                    onToggle = { offset ->
                        val newOffsets = if (uiState.defaultReminderOffsets.contains(offset))
                            uiState.defaultReminderOffsets - offset
                        else
                            uiState.defaultReminderOffsets + offset
                        viewModel.setDefaultReminderOffsets(newOffsets)
                    }
                )
            }
            // 4. 通知
            item {
                SettingsSwitchItem(
                    label = "通知开关",
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
            }
            // 5. 每周起始日
            item {
                SettingsRadioItem(
                    label = "每周起始日",
                    options = WEEK_OPTIONS,
                    selectedValue = uiState.weekStartDay,
                    onSelect = { viewModel.setWeekStartDay(it) }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant) }

            // 6. 导出数据
            item {
                SettingsButtonItem(
                    label = "导出数据",
                    onClick = { exportLauncher.launch("toodo_backup.json") }
                )
            }
            // 7. 导入数据
            item {
                SettingsButtonItem(
                    label = "导入数据",
                    onClick = { importLauncher.launch(arrayOf("application/json")) }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant) }

            // 8. 管理分类
            item {
                SettingsClickableItem(
                    label = "管理分类",
                    value = null,
                    onClick = onManageLists
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant) }

            // 9. 关于
            item {
                SettingsInfoItem(
                    label = "关于",
                    value = "版本 ${uiState.appVersion}"
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  Settings item composables
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun SettingsDropdownItem(label: String, currentValue: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(currentValue, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SettingsClickableItem(label: String, value: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SettingsChipGroupItem(
    label: String, options: List<Pair<Long, String>>, selectedValues: List<Long>, onToggle: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { (offset, name) ->
                FilterChip(selected = selectedValues.contains(offset), onClick = { onToggle(offset) }, label = { Text(name) })
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsSwitchItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsRadioItem(
    label: String, options: List<Pair<Int, String>>, selectedValue: Int, onSelect: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            options.forEach { (value, name) ->
                Row(
                    modifier = Modifier
                        .selectable(selected = selectedValue == value, onClick = { onSelect(value) }, role = Role.RadioButton)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedValue == value, onClick = null)
                    Spacer(Modifier.width(4.dp))
                    Text(name, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsButtonItem(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsInfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
