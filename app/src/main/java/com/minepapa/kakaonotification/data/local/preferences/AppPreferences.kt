package com.minepapa.kakaonotification.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
        val SPREADSHEET_ID  = stringPreferencesKey("SPREADSHEET_ID")
        val SHEET_NAME      = stringPreferencesKey("SHEET_NAME")
        val OAUTH_TOKEN     = stringPreferencesKey("OAUTH_TOKEN")
        val LISTENER_ENABLED = booleanPreferencesKey("LISTENER_ENABLED")
    }

    val spreadsheetId: Flow<String?> = context.dataStore.data.map { it[Keys.SPREADSHEET_ID] }
    val sheetName: Flow<String>      = context.dataStore.data.map { it[Keys.SHEET_NAME] ?: "Notifications" }
    val oauthToken: Flow<String?>    = context.dataStore.data.map { it[Keys.OAUTH_TOKEN] }
    val listenerEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.LISTENER_ENABLED] ?: true }

    suspend fun setSpreadsheetId(id: String) {
        context.dataStore.edit { it[Keys.SPREADSHEET_ID] = id }
    }

    suspend fun setSheetName(name: String) {
        context.dataStore.edit { it[Keys.SHEET_NAME] = name }
    }

    suspend fun setOauthToken(token: String?) {
        context.dataStore.edit {
            if (token != null) it[Keys.OAUTH_TOKEN] = token
            else it.remove(Keys.OAUTH_TOKEN)
        }
    }

    suspend fun setListenerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LISTENER_ENABLED] = enabled }
    }
}
