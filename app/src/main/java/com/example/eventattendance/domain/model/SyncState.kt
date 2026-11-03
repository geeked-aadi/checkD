package com.example.eventattendance.domain.model

sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data class Success(val message: String? = null) : SyncState
    data class Error(val error: String) : SyncState
}
