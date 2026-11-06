package com.example.eventattendance.domain.repository

import com.example.eventattendance.domain.model.UserRole
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentRole: StateFlow<UserRole>
    suspend fun init()
    suspend fun staffLogin(username: String, password: String): Result<UserRole>
    suspend fun logout(): Result<Unit>
    suspend fun ensureSession(): Result<Unit>
}
