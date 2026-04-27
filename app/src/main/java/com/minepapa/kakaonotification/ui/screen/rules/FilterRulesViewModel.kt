package com.minepapa.kakaonotification.ui.screen.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.repository.FilterRuleRepository
import com.minepapa.kakaonotification.domain.usecase.AddFilterRuleUseCase
import com.minepapa.kakaonotification.domain.usecase.DeleteFilterRuleUseCase
import com.minepapa.kakaonotification.domain.usecase.GetFilterRulesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterRulesViewModel @Inject constructor(
    getFilterRulesUseCase: GetFilterRulesUseCase,
    private val addFilterRuleUseCase: AddFilterRuleUseCase,
    private val deleteFilterRuleUseCase: DeleteFilterRuleUseCase,
    private val filterRuleRepository: FilterRuleRepository,
) : ViewModel() {

    val rules = getFilterRulesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addRule(rule: FilterRule) {
        viewModelScope.launch { addFilterRuleUseCase(rule) }
    }

    fun deleteRule(id: Long) {
        viewModelScope.launch { deleteFilterRuleUseCase(id) }
    }

    fun toggleRule(id: Long, enabled: Boolean) {
        viewModelScope.launch { filterRuleRepository.setEnabled(id, enabled) }
    }
}
