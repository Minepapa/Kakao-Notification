package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.model.SelectedSpendingApp
import com.minepapa.kakaonotification.domain.repository.SelectedSpendingAppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSelectedSpendingAppsUseCase @Inject constructor(
    private val repository: SelectedSpendingAppRepository,
) {
    operator fun invoke(): Flow<List<SelectedSpendingApp>> = repository.getSelected()
}
