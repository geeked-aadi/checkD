package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.domain.repository.AuthRepository
import javax.inject.Inject

class StaffLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<UserRole> {
        return authRepository.staffLogin(username, password)
    }
}
