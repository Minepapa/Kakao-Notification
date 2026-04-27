package com.minepapa.kakaonotification.ui.screen.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minepapa.kakaonotification.core.util.NotificationPermissionHelper
import com.minepapa.kakaonotification.data.local.preferences.AppPreferences
import com.minepapa.kakaonotification.domain.usecase.GetFilterRulesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    getFilterRulesUseCase: GetFilterRulesUseCase,
    prefs: AppPreferences,
) : ViewModel() {

    val hasPermission: Boolean
        get() = NotificationPermissionHelper.isGranted(context)

    val ruleCount = getFilterRulesUseCase()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val listenerEnabled = prefs.listenerEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val isSignedIn = prefs.oauthToken
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun openPermissionSettings() {
        NotificationPermissionHelper.openSettings(context)
    }
}
