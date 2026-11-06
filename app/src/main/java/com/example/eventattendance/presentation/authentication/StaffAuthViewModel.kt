package com.example.eventattendance.presentation.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.domain.usecase.StaffLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffAuthViewModel @Inject constructor(
    private val staffLoginUseCase: StaffLoginUseCase
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onUsernameChanged(value: String) {
        _username.value = value
        _errorMessage.value = null
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
        _errorMessage.value = null
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun login(onSuccess: (UserRole) -> Unit) {
        val userVal = _username.value.trim()
        val passwordVal = _password.value

        if (userVal.isBlank() || passwordVal.isBlank()) {
            _errorMessage.value = "Please enter both SPOC ID / Username and password"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = staffLoginUseCase(userVal, passwordVal)
            _isLoading.value = false

            result.onSuccess { role ->
                onSuccess(role)
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Invalid ID or password"
            }
        }
    }
}
