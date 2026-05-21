package com.example.toodo.ui.screen.managelists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toodo.domain.model.TaskList
import com.example.toodo.domain.repository.TaskListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Manage Lists screen.
 */
data class ManageListsUiState(
    val lists: List<TaskList> = emptyList(),
    val isLoading: Boolean = true,
    // Dialog state
    val showDialog: Boolean = false,
    val editingList: TaskList? = null,          // null = creating new
    val dialogName: String = "",
    val dialogColor: Long = 0xFF6750A4,
    val dialogIconName: String = "folder",
    // Delete confirmation
    val showDeleteConfirmation: Boolean = false,
    val deletingList: TaskList? = null
)

@HiltViewModel
class ManageListsViewModel @Inject constructor(
    private val taskListRepository: TaskListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageListsUiState())
    val uiState: StateFlow<ManageListsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            taskListRepository.getAllLists().collect { lists ->
                _uiState.update { it.copy(lists = lists, isLoading = false) }
            }
        }
    }

    // ─── Dialog actions ──────────────────────────────────────────────────────

    /** Open create-new-list dialog. */
    fun showCreateDialog() {
        _uiState.update {
            it.copy(
                showDialog = true,
                editingList = null,
                dialogName = "",
                dialogColor = 0xFF6750A4,
                dialogIconName = "folder"
            )
        }
    }

    /** Open edit-existing-list dialog. */
    fun showEditDialog(list: TaskList) {
        _uiState.update {
            it.copy(
                showDialog = true,
                editingList = list,
                dialogName = list.name,
                dialogColor = list.color,
                dialogIconName = list.iconName
            )
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showDialog = false, editingList = null) }
    }

    fun onDialogNameChanged(name: String) {
        if (name.length <= 30) {
            _uiState.update { it.copy(dialogName = name) }
        }
    }

    fun onDialogColorSelected(color: Long) {
        _uiState.update { it.copy(dialogColor = color) }
    }

    fun onDialogIconSelected(iconName: String) {
        _uiState.update { it.copy(dialogIconName = iconName) }
    }

    // ─── CRUD actions ────────────────────────────────────────────────────────

    fun saveList() {
        val state = _uiState.value
        val name = state.dialogName.trim()
        if (name.isEmpty()) return

        val editing = state.editingList
        val list = TaskList(
            id = editing?.id ?: 0,
            name = name,
            iconName = state.dialogIconName,
            color = state.dialogColor,
            sortOrder = editing?.sortOrder ?: state.lists.size
        )

        viewModelScope.launch {
            if (editing != null) {
                taskListRepository.updateList(list)
            } else {
                taskListRepository.createList(list)
            }
            _uiState.update { it.copy(showDialog = false, editingList = null) }
        }
    }

    // ─── Delete actions ──────────────────────────────────────────────────────

    fun requestDelete(list: TaskList) {
        _uiState.update { it.copy(showDeleteConfirmation = true, deletingList = list) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false, deletingList = null) }
    }

    fun confirmDelete() {
        val list = _uiState.value.deletingList ?: return
        viewModelScope.launch {
            taskListRepository.deleteList(list)
            _uiState.update { it.copy(showDeleteConfirmation = false, deletingList = null) }
        }
    }
}
