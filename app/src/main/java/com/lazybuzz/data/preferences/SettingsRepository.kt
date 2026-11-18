package com.lazybuzz.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lazybuzz.data.models.GestureSettings
import com.lazybuzz.data.models.InteractionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.store: DataStore<Preferences> by preferencesDataStore("settings")

class SettingsRepository(private val context: Context) {
    private val kMode = stringPreferencesKey("mode")
    private val kBend = booleanPreferencesKey("bend_click")
    private val kTap = booleanPreferencesKey("forward_tap")

    val settings: Flow<GestureSettings> = context.store.data.map { p ->
        val d = GestureSettings()
        d.copy(
            mode = p[kMode]?.let { runCatching { InteractionMode.valueOf(it) }.getOrNull() } ?: d.mode,
            bendClickEnabled = p[kBend] ?: d.bendClickEnabled,
            forwardTapEnabled = p[kTap] ?: d.forwardTapEnabled,
        )
    }

    suspend fun setMode(m: InteractionMode) { context.store.edit { it[kMode] = m.name } }
    suspend fun setBendClick(on: Boolean) { context.store.edit { it[kBend] = on } }
    suspend fun setForwardTap(on: Boolean) { context.store.edit { it[kTap] = on } }
}
