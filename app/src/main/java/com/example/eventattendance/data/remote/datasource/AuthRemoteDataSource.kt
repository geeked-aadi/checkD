package com.example.eventattendance.data.remote.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class UserRoleDto(
    val id: String,
    val email: String,
    val role: String = "volunteer"
)

@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun loginWithEmail(email: String, password: String): Boolean {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return true
    }

    suspend fun isUserAdmin(): Boolean {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return false
        val userRow = supabaseClient.from("users")
            .select {
                filter {
                    eq("id", currentUserId)
                }
            }
            .decodeSingleOrNull<UserRoleDto>()
        return userRow?.role.equals("admin", ignoreCase = true)
    }

    suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return supabaseClient.auth.currentUserOrNull() != null
    }

    fun getCurrentUserEmail(): String? {
        return supabaseClient.auth.currentUserOrNull()?.email
    }
}
