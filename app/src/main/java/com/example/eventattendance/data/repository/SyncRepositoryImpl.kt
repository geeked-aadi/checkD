package com.example.eventattendance.data.repository

import com.example.eventattendance.data.local.AppDatabase
import com.example.eventattendance.domain.model.SyncState
import com.example.eventattendance.domain.repository.SyncRepository
import com.example.eventattendance.sync.ConnectivityObserver
import com.example.eventattendance.sync.NetworkConnectivityObserver
import com.example.eventattendance.sync.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val syncManager: SyncManager,
    private val connectivityObserver: NetworkConnectivityObserver,
    private val database: AppDatabase
) : SyncRepository {

    override val syncState: StateFlow<SyncState> = syncManager.syncState

    override fun observeConnectivity(): Flow<ConnectivityObserver.Status> {
        return connectivityObserver.observe()
    }

    override fun getTotalPendingScansCountFlow(): Flow<Int> {
        return database.pendingScanDao().getTotalPendingCountFlow()
    }

    override suspend fun syncNow(forcePull: Boolean): Result<Unit> {
        return syncManager.syncAll(forcePull)
    }
}
