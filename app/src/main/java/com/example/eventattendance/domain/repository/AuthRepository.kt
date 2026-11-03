package com.example.eventattendance.domain.repository

interface AuthRepository {
    suspend fun loginAsAdmin(email: String, password: String): Result<Boolean>
    suspend fun logout(): Result<Unit>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserEmail(): String?
}
