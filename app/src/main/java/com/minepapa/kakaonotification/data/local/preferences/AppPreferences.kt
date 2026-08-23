package com.minepapa.kakaonotification.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val LISTENER_ENABLED = booleanPreferencesKey("LISTENER_ENABLED")
    }

    val listenerEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.LISTENER_ENABLED] ?: true
    }

    suspend fun setListenerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LISTENER_ENABLED] = enabled }
    }
}
