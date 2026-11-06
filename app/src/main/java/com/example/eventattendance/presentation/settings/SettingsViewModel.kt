package com.example.eventattendance.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.domain.repository.AuthRepository
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import com.example.eventattendance.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val logoutUseCase: LogoutUseCase,
    private val participantCacheRepository: ParticipantCacheRepository
) : ViewModel() {

    private val _isHapticEnabled = MutableStateFlow(true)
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _isRefreshingCache = MutableStateFlow(false)
    val isRefreshingCache: StateFlow<Boolean> = _isRefreshingCache.asStateFlow()

    private val _cacheRefreshMessage = MutableSharedFlow<String>()
    val cacheRefreshMessage: SharedFlow<String> = _cacheRefreshMessage.asSharedFlow()

    val currentRole: StateFlow<UserRole> = authRepository.currentRole

    fun toggleHaptic(enabled: Boolean) {
        _isHapticEnabled.value = enabled
    }

    fun toggleSound(enabled: Boolean) {
        _isSoundEnabled.value = enabled
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

    fun logout(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            logoutUseCase()
            onSuccess()
        }
    }
}
