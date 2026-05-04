package com.minepapa.kakaonotification.ui.screen.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minepapa.kakaonotification.domain.repository.NotificationLogRepository
import com.minepapa.kakaonotification.domain.usecase.DeleteNotificationLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationLogViewModel @Inject constructor(
    repo: NotificationLogRepository,
    private val deleteLogsUseCase: DeleteNotificationLogsUseCase,
) : ViewModel() {

    val logs = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectionMode = MutableStateFlow(false)
    val selectionMode = _selectionMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    fun enterSelectionMode() {
        _selectionMode.value = true
        _selectedIds.value = emptySet()
    }

    fun exitSelectionMode() {
        _selectionMode.value = false
        _selectedIds.value = emptySet()
    }

    fun toggleSelection(id: Long) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply {
            if (contains(id)) remove(id) else add(id)
        }
    }

    fun selectAll() {
        val allIds = logs.value.map { it.id }.toSet()
        _selectedIds.value = if (_selectedIds.value == allIds) emptySet() else allIds
    }

    fun deleteSelected() {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            deleteLogsUseCase(ids)
            exitSelectionMode()
        }
    }
}
