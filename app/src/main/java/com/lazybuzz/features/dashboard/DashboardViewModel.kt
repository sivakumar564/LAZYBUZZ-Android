package com.lazybuzz.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lazybuzz.data.models.GestureSettings
import com.lazybuzz.data.models.InteractionMode
import com.lazybuzz.data.preferences.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(private val repo: SettingsRepository) : ViewModel() {
    val settings = repo.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GestureSettings())

    fun setMode(m: InteractionMode) { viewModelScope.launch { repo.setMode(m) } }
    fun setBendClick(on: Boolean) { viewModelScope.launch { repo.setBendClick(on) } }
    fun setForwardTap(on: Boolean) { viewModelScope.launch { repo.setForwardTap(on) } }

    companion object {
        fun factory(repo: SettingsRepository) = viewModelFactory { initializer { DashboardViewModel(repo) } }
    }
}
