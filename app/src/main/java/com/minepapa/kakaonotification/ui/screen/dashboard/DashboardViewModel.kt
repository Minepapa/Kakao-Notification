package com.minepapa.kakaonotification.ui.screen.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minepapa.kakaonotification.core.util.NotificationPermissionHelper
import com.minepapa.kakaonotification.data.remote.auth.FirebaseAuthProvider
import com.minepapa.kakaonotification.domain.usecase.GetFilterRulesUseCase
import com.minepapa.kakaonotification.domain.usecase.GetSelectedSpendingAppsUseCase
import com.minepapa.kakaonotification.domain.usecase.buildRuleSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    getFilterRulesUseCase: GetFilterRulesUseCase,
    getSelectedSpendingAppsUseCase: GetSelectedSpendingAppsUseCase,
    private val authProvider: FirebaseAuthProvider,
) : ViewModel() {

    val hasPermission: Boolean
        get() = NotificationPermissionHelper.isGranted(context)

    val ruleSummary = getFilterRulesUseCase()
        .map { buildRuleSummary(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), buildRuleSummary(emptyList()))

    val selectedSpendingApps = getSelectedSpendingAppsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isSignedIn = MutableStateFlow(authProvider.isSignedIn())
    val isSignedIn = _isSignedIn.asStateFlow()

    fun refreshSignInState() {
        _isSignedIn.value = authProvider.isSignedIn()
    }

    fun openPermissionSettings() {
        NotificationPermissionHelper.openSettings(context)
    }
}
