package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.repository.AuthRepository
import javax.inject.Inject

class AdminLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun login(email: String, password: String): Result<Boolean> {
        return authRepository.loginAsAdmin(email, password)
    }

    suspend fun logout(): Result<Unit> {
        return authRepository.logout()
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    fun getCurrentEmail(): String? {
        return authRepository.getCurrentUserEmail()
    }
}
