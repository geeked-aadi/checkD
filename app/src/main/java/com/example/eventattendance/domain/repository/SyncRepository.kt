package com.example.eventattendance.domain.repository

import com.example.eventattendance.domain.model.SyncState
import com.example.eventattendance.sync.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SyncRepository {
    val syncState: StateFlow<SyncState>
    fun observeConnectivity(): Flow<ConnectivityObserver.Status>
    fun getTotalPendingScansCountFlow(): Flow<Int>
    suspend fun syncNow(forcePull: Boolean = false): Result<Unit>
}
