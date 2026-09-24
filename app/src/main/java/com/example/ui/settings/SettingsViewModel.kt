package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.PreferencesManager
import com.example.data.model.DialerSettings
import com.example.data.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesManager: PreferencesManager = SalimApplication.instance.preferencesManager
) : ViewModel() {

    val settings: StateFlow<DialerSettings> = preferencesManager
        .settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DialerSettings())

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateSettings {
                it.copy(themeMode = if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
            }
        }
    }

    fun toggleT9Search(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(t9SearchEnabled = enabled) }
        }
    }

    fun toggleDialpadTones(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(dialpadTones = enabled) }
        }
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(hapticFeedback = enabled) }
        }
    }

    fun toggleBlockUnknown(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(blockUnknownNumbers = enabled) }
        }
    }

    fun setDefaultStartTab(tab: String) {
        viewModelScope.launch {
            preferencesManager.updateDefaultStartTab(tab)
        }
    }

    fun setCallBackgroundUri(uri: String?) {
        viewModelScope.launch {
            preferencesManager.updateCallBackgroundUri(uri)
        }
    }
}
