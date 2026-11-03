package com.example.eventattendance.data.repository

import com.example.eventattendance.data.remote.datasource.AuthRemoteDataSource
import com.example.eventattendance.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun loginAsAdmin(email: String, password: String): Result<Boolean> {
        return try {
            authRemoteDataSource.loginWithEmail(email, password)
            val isAdmin = authRemoteDataSource.isUserAdmin()
            if (!isAdmin) {
                authRemoteDataSource.signOut()
                Result.failure(Exception("Access denied: You do not have administrator privileges."))
            } else {
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            authRemoteDataSource.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return authRemoteDataSource.isUserLoggedIn()
    }

    override fun getCurrentUserEmail(): String? {
        return authRemoteDataSource.getCurrentUserEmail()
    }
}
