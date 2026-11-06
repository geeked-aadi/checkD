package com.example.eventattendance.domain.model

sealed interface ScanResult {
    data class Success(
        val participantName: String,
        val participantUsn: String,
        val teamName: String? = null,
        val isPendingSync: Boolean = false,
        val timestamp: String? = null
    ) : ScanResult

    data class AlreadyCheckedIn(
        val participantName: String,
        val participantUsn: String,
        val teamName: String? = null
    ) : ScanResult

    data object InvalidQr : ScanResult

    data class NotEligible(
        val participantName: String? = null,
        val participantUsn: String? = null
    ) : ScanResult

    data object EventClosed : ScanResult

    data object NetworkError : ScanResult

    data class Error(val message: String) : ScanResult
}
