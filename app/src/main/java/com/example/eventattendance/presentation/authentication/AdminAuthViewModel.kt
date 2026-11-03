package com.example.eventattendance.presentation.authentication

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
class AdminAuthViewModel @Inject constructor(
    private val adminLoginUseCase: AdminLoginUseCase
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onEmailChanged(value: String) {
        _email.value = value
        _errorMessage.value = null
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
        _errorMessage.value = null
    }

    fun login(onSuccess: () -> Unit) {
        val emailVal = _email.value.trim()
        val passwordVal = _password.value

        if (emailVal.isBlank() || passwordVal.isBlank()) {
            _errorMessage.value = "Please enter both email and password"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = adminLoginUseCase.login(emailVal, passwordVal)
            _isLoading.value = false

            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Admin verification failed"
            }
        }
    }
}
