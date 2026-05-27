package com.minepapa.kakaonotification.ui.screen.dividend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minepapa.kakaonotification.data.local.preferences.AppPreferences
import com.minepapa.kakaonotification.domain.model.DividendSheetEntry
import com.minepapa.kakaonotification.domain.repository.DividendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DividendListViewModel @Inject constructor(
    private val dividendRepository: DividendRepository,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val _entries = MutableStateFlow<List<DividendSheetEntry>>(emptyList())
    val entries = _entries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    init {
        loadEntries()
    }

    fun loadEntries() {
        viewModelScope.launch {
            _isLoading.value = true
            val spreadsheetId = prefs.spreadsheetId.first()
            if (spreadsheetId.isBlank()) {
                _message.value = "스프레드시트 ID가 설정되지 않았습니다"
                _isLoading.value = false
                return@launch
            }
            dividendRepository.getDividends(spreadsheetId).fold(
                onSuccess = { _entries.value = it },
                onFailure = { _message.value = "오류: ${it.message}" },
            )
            _isLoading.value = false
        }
    }

    fun updateStockName(rowNumber: Int, newName: String) {
        viewModelScope.launch {
            val spreadsheetId = prefs.spreadsheetId.first()
            dividendRepository.updateStockName(spreadsheetId, rowNumber, newName).fold(
                onSuccess = {
                    _entries.value = _entries.value.map { entry ->
                        if (entry.rowNumber == rowNumber) entry.copy(stockName = newName) else entry
                    }
                    _message.value = "종목명 수정 완료"
                    delay(2000)
                    _message.value = null
                },
                onFailure = {
                    _message.value = "오류: ${it.message}"
                    delay(5000)
                    _message.value = null
                },
            )
        }
    }
}
