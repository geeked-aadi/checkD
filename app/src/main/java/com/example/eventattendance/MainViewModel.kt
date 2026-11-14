package com.example.eventattendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.model.ThemeMode
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.domain.repository.AuthRepository
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import com.example.eventattendance.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val participantCacheRepository: ParticipantCacheRepository,
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    val currentRole: StateFlow<UserRole> = authRepository.currentRole

    val themeMode: StateFlow<ThemeMode> = preferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )

    init {
        viewModelScope.launch {
            runCatching {
                authRepository.init()
                participantCacheRepository.initializeCache()
            }
            _isReady.value = true
        }
    }
}
