package com.minepapa.kakaonotification.domain.repository

import com.minepapa.kakaonotification.domain.model.SelectedSpendingApp
import kotlinx.coroutines.flow.Flow

interface SelectedSpendingAppRepository {
    fun getSelected(): Flow<List<SelectedSpendingApp>>
}
