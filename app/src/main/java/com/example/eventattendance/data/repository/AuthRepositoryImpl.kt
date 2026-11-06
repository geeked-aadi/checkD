package com.example.eventattendance.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.eventattendance.data.remote.datasource.AuthRemoteDataSource
import com.example.eventattendance.di.IoDispatcher
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.domain.repository.AuthRepository
import com.example.eventattendance.sync.ConnectivityObserver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val dataStore: DataStore<Preferences>,
    private val connectivityObserver: ConnectivityObserver,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    private val _currentRole = MutableStateFlow<UserRole>(UserRole.Volunteer)
    override val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val repositoryScope = CoroutineScope(ioDispatcher)

    init {
        // Observe connectivity changes to auto-retry anonymous sign-in if offline during start
        repositoryScope.launch {
            connectivityObserver.observe().collect { status ->
                if (status == ConnectivityObserver.Status.Available) {
                    ensureSession()
                }
            }
        }
    }

    override suspend fun init(): Unit = withContext(ioDispatcher) {
        // 1. Await Supabase auth initialization and ensure anonymous session if needed
        runCatching {
            authRemoteDataSource.awaitInitialization()
            if (authRemoteDataSource.currentSessionOrNull() == null) {
                authRemoteDataSource.signInAnonymously()
            }
        }

        // 2. Restore staff role from DataStore
        runCatching {
            val prefs = dataStore.data.first()
            val savedRole = prefs[STAFF_ROLE_KEY]
            val savedSpocId = prefs[STAFF_SPOC_ID_KEY]

            val restoredRole: UserRole = when {
                savedRole.equals("admin", ignoreCase = true) -> UserRole.Admin
                savedRole.equals("spoc", ignoreCase = true) && !savedSpocId.isNullOrBlank() -> UserRole.Spoc(savedSpocId)
                else -> UserRole.Volunteer
            }
            _currentRole.value = restoredRole
        }.onFailure {
            _currentRole.value = UserRole.Volunteer
        }
        Unit
    }

    override suspend fun staffLogin(username: String, password: String): Result<UserRole> = withContext(ioDispatcher) {
        try {
            val trimmedUsername = username.trim()
            if (trimmedUsername.isBlank() || password.isBlank()) {
                throw Exception("Invalid ID or password")
            }

            // Ensure we have an active session for the RPC call
            if (authRemoteDataSource.currentSessionOrNull() == null) {
                authRemoteDataSource.signInAnonymously()
            }

            val result = authRemoteDataSource.staffLogin(trimmedUsername, password)
            if (!result.ok) {
                throw Exception("Invalid ID or password")
            }

            val roleString = result.role?.lowercase()
            val determinedRole: UserRole = when (roleString) {
                "admin" -> UserRole.Admin
                "spoc" -> {
                    val spocId = result.spocId?.takeIf { it.isNotBlank() } ?: trimmedUsername
                    UserRole.Spoc(spocId)
                }
                else -> throw Exception("Invalid ID or password")
            }

            // Save role & spoc_id to DataStore
            dataStore.edit { prefs ->
                when (determinedRole) {
                    is UserRole.Admin -> {
                        prefs[STAFF_ROLE_KEY] = "admin"
                        prefs.remove(STAFF_SPOC_ID_KEY)
                    }
                    is UserRole.Spoc -> {
                        prefs[STAFF_ROLE_KEY] = "spoc"
                        prefs[STAFF_SPOC_ID_KEY] = determinedRole.spocId
                    }
                    is UserRole.Volunteer -> {
                        prefs.remove(STAFF_ROLE_KEY)
                        prefs.remove(STAFF_SPOC_ID_KEY)
                    }
                }
            }

            _currentRole.value = determinedRole
            Result.success(determinedRole)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(ioDispatcher) {
        try {
            dataStore.edit { prefs ->
                prefs.remove(STAFF_ROLE_KEY)
                prefs.remove(STAFF_SPOC_ID_KEY)
            }
            _currentRole.value = UserRole.Volunteer
            // Keep the anonymous session active so scans continue working
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun ensureSession(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            if (authRemoteDataSource.currentSessionOrNull() == null) {
                authRemoteDataSource.signInAnonymously()
            }
            Unit
        }
    }

    companion object {
        val STAFF_ROLE_KEY = stringPreferencesKey("staff_role")
        val STAFF_SPOC_ID_KEY = stringPreferencesKey("staff_spoc_id")
    }
}
