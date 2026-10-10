package com.minepapa.kakaonotification.data.repository

import android.content.Context
import android.content.pm.PackageManager
import com.minepapa.kakaonotification.data.local.preferences.AppPreferences
import com.minepapa.kakaonotification.domain.model.SelectedSpendingApp
import com.minepapa.kakaonotification.domain.repository.SelectedSpendingAppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SelectedSpendingAppRepositoryImpl @Inject constructor(
    private val preferences: AppPreferences,
    @ApplicationContext private val context: Context,
) : SelectedSpendingAppRepository {
    override fun getSelected(): Flow<List<SelectedSpendingApp>> =
        preferences.selectedSpendingPackages.map { packages ->
            val packageManager = context.packageManager
            packages.map { packageName ->
                val label = try {
                    packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(packageName, 0)
                    ).toString()
                } catch (_: PackageManager.NameNotFoundException) {
                    packageName
                }
                SelectedSpendingApp(packageName, label)
            }.sortedBy { it.label.lowercase() }
        }.flowOn(Dispatchers.IO)
}
