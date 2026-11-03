package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.model.SyncState
import com.example.eventattendance.domain.repository.SyncRepository
import com.example.eventattendance.sync.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SyncDataUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    val syncState: StateFlow<SyncState> = syncRepository.syncState

    fun observeConnectivity(): Flow<ConnectivityObserver.Status> {
        return syncRepository.observeConnectivity()
    }

    fun getPendingCount(): Flow<Int> {
        return syncRepository.getTotalPendingScansCountFlow()
    }

    suspend fun triggerSync(forcePull: Boolean = false): Result<Unit> {
        return syncRepository.syncNow(forcePull)
    }
}
