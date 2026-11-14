package com.example.eventattendance.sync

import com.example.eventattendance.domain.model.SyncState
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository
import com.example.eventattendance.domain.repository.AttendanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val connectivityObserver: NetworkConnectivityObserver,
    private val attendanceRepositoryProvider: Provider<AttendanceRepository>,
    private val attendanceMatrixRepositoryProvider: Provider<AttendanceMatrixRepository>
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)
    val refreshTrigger: SharedFlow<Unit> = _refreshTrigger.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        _refreshTrigger.tryEmit(Unit)
        scope.launch {
            connectivityObserver.observe().collect { status ->
                if (status == ConnectivityObserver.Status.Available) {
                    syncAll()
                }
            }
        }
    }

    suspend fun syncAll(forcePull: Boolean = false): Result<Unit> {
        return try {
            _syncState.value = SyncState.Syncing
            runCatching {
                attendanceRepositoryProvider.get().syncAllEventsAttendance(forcePull = true)
            }
            runCatching {
                attendanceMatrixRepositoryProvider.get().refresh()
            }
            _refreshTrigger.emit(Unit)
            _syncState.value = SyncState.Success("Synced with cloud")
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e.message ?: "Sync failed")
            Result.failure(e)
        }
    }
}
