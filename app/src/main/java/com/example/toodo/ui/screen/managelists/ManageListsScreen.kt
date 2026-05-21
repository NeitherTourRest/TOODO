package com.example.toodo.ui.screen.managelists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.toodo.domain.model.TaskList

/** 8 preset colors for the category color picker. */
private val PRESET_COLORS = listOf(
    0xFFFF5252L, // Red
    0xFFFFB74DL, // Orange
    0xFFFFD600L, // Yellow
    0xFF42A5F5L, // Blue
    0xFF26A69AL, // Teal
    0xFF5B5BD6L, // Indigo
    0xFF6750A4L, // Purple (default)
    0xFFEC407AL  // Pink
)

/** 8 preset icons for the category icon picker (name → ImageVector). */
private val PRESET_ICONS = listOf(
    "folder" to Icons.Default.Folder,
    "star" to Icons.Default.Star,
    "home" to Icons.Default.Home,
    "work" to Icons.Default.WorkOutline,
    "school" to Icons.Default.School,
    "fitness_center" to Icons.Default.FitnessCenter,
    "shopping_cart" to Icons.Default.ShoppingCart,
    "bookmark" to Icons.Default.Bookmark
)

private fun iconForName(name: String): ImageVector =
    PRESET_ICONS.firstOrNull { it.first == name }?.second ?: Icons.Default.Folder

// ═════════════════════════════════════════════════════════════════════════════
//  ManageListsScreen
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageListsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: ManageListsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // ── Delete confirmation dialog ───────────────────────────────────────────
    if (uiState.showDeleteConfirmation && uiState.deletingList != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirmation() },
            title = { Text("删除分类") },
            text = { Text("确定要删除「${uiState.deletingList!!.name}」吗？该分类下的任务不会被删除。") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirmation() }) { Text("取消") }
            }
        )
    }

    // ── Create / Edit dialog ─────────────────────────────────────────────────
    if (uiState.showDialog) {
        EditListDialog(
            isEditing = uiState.editingList != null,
            name = uiState.dialogName,
            selectedColor = uiState.dialogColor,
            selectedIconName = uiState.dialogIconName,
            onNameChange = { viewModel.onDialogNameChanged(it) },
            onColorSelect = { viewModel.onDialogColorSelected(it) },
            onIconSelect = { viewModel.onDialogIconSelected(it) },
            onConfirm = { viewModel.saveList() },
            onDismiss = { viewModel.dismissDialog() }
        )
    }

    // ── Main scaffold ────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("管理分类") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("添加分类") }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.lists.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FolderOpen, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("暂无分类", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("点击下方按钮添加你的第一个分类", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp)
            ) {
                items(uiState.lists, key = { it.id }) { list ->
                    CategoryRow(
                        list = list,
                        onEdit = { viewModel.showEditDialog(list) },
                        onDelete = { viewModel.requestDelete(list) }
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  Category row
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun CategoryRow(list: TaskList, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Color dot with icon
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(list.color)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForName(list.iconName),
                    contentDescription = list.name,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(list.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            // Edit button
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // Delete button
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  Create / Edit dialog
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun EditListDialog(
    isEditing: Boolean,
    name: String,
    selectedColor: Long,
    selectedIconName: String,
    onNameChange: (String) -> Unit,
    onColorSelect: (Long) -> Unit,
    onIconSelect: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "编辑分类" else "新建分类") },
        text = {
            Column {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // Color grid
                Text("颜色", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    PRESET_COLORS.forEach { color ->
                        val isSelected = color == selectedColor
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { onColorSelect(color) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Icon grid
                Text("图标", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    PRESET_ICONS.forEach { (iconName, iconVector) ->
                        val isSelected = iconName == selectedIconName
                        IconButton(
                            onClick = { onIconSelect(iconName) },
                            modifier = Modifier
                                .size(40.dp)
                                .then(
                                    if (isSelected) Modifier.clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                    else Modifier
                                )
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = iconName,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.trim().isNotEmpty()) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
