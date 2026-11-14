package com.example.eventattendance.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.model.ThemeMode
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.domain.repository.AuthRepository
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import com.example.eventattendance.domain.repository.UserPreferencesRepository
import com.example.eventattendance.domain.usecase.ClearLocalCacheUseCase
import com.example.eventattendance.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val logoutUseCase: LogoutUseCase,
    private val participantCacheRepository: ParticipantCacheRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clearLocalCacheUseCase: ClearLocalCacheUseCase
) : ViewModel() {


    val themeMode: StateFlow<ThemeMode> = userPreferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )

    val isHapticEnabled: StateFlow<Boolean> = userPreferencesRepository.isHapticEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val isSoundEnabled: StateFlow<Boolean> = userPreferencesRepository.isSoundEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    private val _isRefreshingCache = MutableStateFlow(false)
    val isRefreshingCache: StateFlow<Boolean> = _isRefreshingCache.asStateFlow()

    private val _isClearingCache = MutableStateFlow(false)
    val isClearingCache: StateFlow<Boolean> = _isClearingCache.asStateFlow()

    private val _cacheRefreshMessage = MutableSharedFlow<String>()
    val cacheRefreshMessage: SharedFlow<String> = _cacheRefreshMessage.asSharedFlow()

    val currentRole: StateFlow<UserRole> = authRepository.currentRole

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun toggleHaptic(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setHapticEnabled(enabled)
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setSoundEnabled(enabled)
        }
    }

    fun refreshParticipantsCache() {
        viewModelScope.launch {
            _isRefreshingCache.value = true
            participantCacheRepository.refreshParticipants()
                .onSuccess {
                    _cacheRefreshMessage.emit("Participants cache refreshed successfully")
                }
                .onFailure { error ->
                    _cacheRefreshMessage.emit(error.message ?: "Failed to refresh participants cache")
                }
            _isRefreshingCache.value = false
        }
    }

    fun clearAllLocalCache() {
        viewModelScope.launch {
            _isClearingCache.value = true
            clearLocalCacheUseCase()
                .onSuccess {
                    _cacheRefreshMessage.emit("Local cache cleared & resynced with database")
                }
                .onFailure { error ->
                    _cacheRefreshMessage.emit(error.message ?: "Failed to clear cache")
                }
            _isClearingCache.value = false
        }
    }


    fun logout(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            logoutUseCase()
            onSuccess()
        }
    }
}
