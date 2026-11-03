package com.example.eventattendance.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.usecase.AdminLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val adminLoginUseCase: AdminLoginUseCase
) : ViewModel() {

    private val _isHapticEnabled = MutableStateFlow(true)
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    val isAdminLoggedIn: Boolean
        get() = adminLoginUseCase.isUserLoggedIn()

    val adminEmail: String?
        get() = adminLoginUseCase.getCurrentEmail()

    fun toggleHaptic(enabled: Boolean) {
        _isHapticEnabled.value = enabled
    }

    fun toggleSound(enabled: Boolean) {
        _isSoundEnabled.value = enabled
    }

    fun logoutAdmin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            adminLoginUseCase.logout()
            onSuccess()
        }
    }
}
