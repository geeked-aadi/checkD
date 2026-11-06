package com.example.eventattendance.data.remote.datasource

import com.example.eventattendance.data.remote.dto.StaffLoginRequest
import com.example.eventattendance.data.remote.dto.StaffLoginResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun awaitInitialization() {
        supabaseClient.auth.awaitInitialization()
    }

    fun currentSessionOrNull(): UserSession? {
        return supabaseClient.auth.currentSessionOrNull()
    }

    suspend fun signInAnonymously() {
        supabaseClient.auth.signInAnonymously()
    }

    suspend fun staffLogin(username: String, password: String): StaffLoginResult {
        return supabaseClient.postgrest.rpc(
            function = "staff_login",
            parameters = StaffLoginRequest(
                p_username = username,
                p_password = password
            )
        ).decodeAs<StaffLoginResult>()
    }

    fun isUserLoggedIn(): Boolean {
        return supabaseClient.auth.currentSessionOrNull() != null
    }

    fun getCurrentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }
}
