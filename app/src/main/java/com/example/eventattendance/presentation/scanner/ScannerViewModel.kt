package com.example.eventattendance.presentation.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.model.AttendanceRecord
import com.example.eventattendance.domain.model.ScanResult
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository
import com.example.eventattendance.domain.repository.AttendanceRepository
import com.example.eventattendance.domain.repository.AuthRepository
import com.example.eventattendance.domain.usecase.LookupParticipantUseCase
import com.example.eventattendance.domain.usecase.MarkAttendanceUseCase
import com.example.eventattendance.util.AudioFeedbackUtil
import com.example.eventattendance.util.DateUtils
import com.example.eventattendance.util.HapticFeedbackUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

data class PendingAttendee(
    val pId: String,
    val name: String,
    val teamName: String? = null,
    val teamId: String? = null
)

data class BulkSummary(
    val markedCount: Int,
    val duplicateNames: List<String>
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val lookupParticipantUseCase: LookupParticipantUseCase,
    private val markAttendanceUseCase: MarkAttendanceUseCase,
    private val attendanceRepository: AttendanceRepository,
    private val attendanceMatrixRepository: AttendanceMatrixRepository,
    private val authRepository: AuthRepository,
    private val hapticFeedbackUtil: HapticFeedbackUtil,
    private val audioFeedbackUtil: AudioFeedbackUtil,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val eventId: String = checkNotNull(savedStateHandle["eventId"])
    val eventIdInt: Int = eventId.toIntOrNull() ?: 0
    val isBulkMode: Boolean = savedStateHandle["bulk"] ?: false

    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _isBackCamera = MutableStateFlow(true)
    val isBackCamera: StateFlow<Boolean> = _isBackCamera.asStateFlow()

    // Bulk Scan State
    private val _pendingList = MutableStateFlow<List<PendingAttendee>>(emptyList())
    val pendingList: StateFlow<List<PendingAttendee>> = _pendingList.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _bulkSummary = MutableStateFlow<BulkSummary?>(null)
    val bulkSummary: StateFlow<BulkSummary?> = _bulkSummary.asStateFlow()

    private val _bulkHint = MutableStateFlow<String?>(null)
    val bulkHint: StateFlow<String?> = _bulkHint.asStateFlow()

    private var lastScannedToken: String? = null
    private var lastScanTimestamp: Long = 0
    private var dismissBannerJob: Job? = null
    private var dismissHintJob: Job? = null
    private val scanMutex = Mutex()

    // Debounce: 1200ms for same token, 500ms general
    private val debounceSameTokenMs = 1200L
    private val debounceGeneralMs = 500L

    fun onQrCodeScanned(qrToken: String) {
        val trimmedToken = qrToken.trim()
        if (trimmedToken.isBlank()) return

        val now = System.currentTimeMillis()
        if (trimmedToken == lastScannedToken && (now - lastScanTimestamp) < debounceSameTokenMs) {
            return
        }
        if ((now - lastScanTimestamp) < debounceGeneralMs) {
            return
        }

        lastScannedToken = trimmedToken
        lastScanTimestamp = now

        if (isBulkMode) {
            handleBulkScan(trimmedToken)
        } else {
            handleSingleScan(trimmedToken)
        }
    }

    private fun handleSingleScan(token: String) {
        viewModelScope.launch {
            if (!scanMutex.tryLock()) {
                return@launch // Scan already in flight, prevent duplicate concurrent dispatch
            }
            try {
                val participant = lookupParticipantUseCase(token)
                if (participant == null) {
                    hapticFeedbackUtil.vibrateError()
                    audioFeedbackUtil.playBeepError()
                    showFeedback(ScanResult.InvalidQr)
                    return@launch
                }

            // Scoped mode validation: if user is SPOC, participant must belong to this SPOC
            val currentRole = authRepository.currentRole.value
            if (currentRole is UserRole.Spoc) {
                if (participant.spocId != currentRole.spocId) {
                    hapticFeedbackUtil.vibrateError()
                    audioFeedbackUtil.playBeepError()
                    showFeedback(ScanResult.Error("Not in your teams"))
                    return@launch
                }
            }

            val result = markAttendanceUseCase(eventIdInt, listOf(participant.pId))
            result.onSuccess { response ->
                when (response.status) {
                    "success" -> {
                        hapticFeedbackUtil.vibrateSuccess()
                        audioFeedbackUtil.playBeepSuccess()
                        val nowIso = DateUtils.getCurrentIsoTime()
                        attendanceRepository.recordScanSuccess(
                            eventId = eventIdInt,
                            participantId = participant.pId,
                            name = participant.name ?: participant.pId,
                            teamId = participant.teamId,
                            teamName = participant.teamName,
                            currentTime = nowIso,
                            progress = response.progress,
                            newAttendance = response.eventAttendance,
                            total = response.totalParticipants
                        )
                        // Immediate optimistic update to attendance matrix
                        attendanceMatrixRepository.addLocalScan(eventIdInt, participant.pId)

                        showFeedback(
                            ScanResult.Success(
                                participantName = participant.name ?: participant.pId,
                                participantUsn = participant.pId,
                                teamName = participant.teamName
                            )
                        )
                    }
                    "duplicate" -> {
                        hapticFeedbackUtil.vibrateError()
                        audioFeedbackUtil.playBeepError()
                        // Ensure matrix also includes this participant on duplicate
                        attendanceMatrixRepository.addLocalScan(eventIdInt, participant.pId)

                        showFeedback(
                            ScanResult.AlreadyCheckedIn(
                                participantName = participant.name ?: participant.pId,
                                participantUsn = participant.pId,
                                teamName = participant.teamName
                            )
                        )
                    }
                    "error" -> {
                        hapticFeedbackUtil.vibrateError()
                        audioFeedbackUtil.playBeepError()
                        showFeedback(ScanResult.Error(response.message ?: "Attendance rejected"))
                    }
                    else -> {
                        hapticFeedbackUtil.vibrateError()
                        audioFeedbackUtil.playBeepError()
                        showFeedback(ScanResult.Error(response.message ?: "Attendance failed"))
                    }
                }
            }.onFailure { throwable ->
                hapticFeedbackUtil.vibrateError()
                audioFeedbackUtil.playBeepError()
                val isNetwork = throwable is java.io.IOException ||
                    throwable is java.net.SocketException ||
                    throwable is java.net.UnknownHostException
                if (isNetwork) {
                    showFeedback(ScanResult.NetworkError)
                } else {
                    val msg = sanitizeErrorMessage(throwable.message, "Attendance recording failed")
                    showFeedback(ScanResult.Error(msg))
                }
            }
        } finally {
            scanMutex.unlock()
        }
    }
}

    private fun handleBulkScan(token: String) {
        viewModelScope.launch {
            val participant = lookupParticipantUseCase(token)
            if (participant == null) {
                hapticFeedbackUtil.vibrateError()
                audioFeedbackUtil.playBeepError()
                showFeedback(ScanResult.InvalidQr)
                return@launch
            }

            // Scoped mode validation: if user is SPOC, participant must belong to this SPOC
            val currentRole = authRepository.currentRole.value
            if (currentRole is UserRole.Spoc) {
                if (participant.spocId != currentRole.spocId) {
                    hapticFeedbackUtil.vibrateError()
                    audioFeedbackUtil.playBeepError()
                    showBulkHint("Not in your teams (${participant.name ?: participant.pId})")
                    return@launch
                }
            }

            if (_pendingList.value.any { it.pId == participant.pId }) {
                hapticFeedbackUtil.vibrateError()
                audioFeedbackUtil.playBeepError()
                showBulkHint("${participant.name ?: participant.pId} already added to queue")
                return@launch
            }

            val attendee = PendingAttendee(
                pId = participant.pId,
                name = participant.name ?: participant.pId,
                teamName = participant.teamName,
                teamId = participant.teamId
            )
            _pendingList.value = _pendingList.value + attendee
            hapticFeedbackUtil.vibrateSuccess()
            audioFeedbackUtil.playBeepSuccess()
        }
    }

    fun removePendingAttendee(pId: String) {
        _pendingList.value = _pendingList.value.filterNot { it.pId == pId }
    }

    fun clearPendingList() {
        _pendingList.value = emptyList()
    }

    fun dismissBulkSummary() {
        _bulkSummary.value = null
    }

    fun submitBulkAttendance() {
        val currentPending = _pendingList.value
        if (currentPending.isEmpty() || _isSubmitting.value) return

        viewModelScope.launch {
            if (!scanMutex.tryLock()) return@launch
            try {
                _isSubmitting.value = true
                val pIds = currentPending.map { it.pId }
                val result = markAttendanceUseCase(eventIdInt, pIds)

                result.onSuccess { response ->
                    if (response.status == "error") {
                        hapticFeedbackUtil.vibrateError()
                        audioFeedbackUtil.playBeepError()
                        showFeedback(ScanResult.Error(response.message ?: "Bulk attendance rejected"))
                    } else {
                        val markedIds = response.markedPresent.toSet()
                        val duplicateIds = response.duplicates.toSet()
                        val nowIso = DateUtils.getCurrentIsoTime()

                        val markedAttendees = currentPending.filter { it.pId in markedIds }.map {
                            AttendanceRecord(
                                id = "${eventIdInt}_${it.pId}",
                                eventId = eventId,
                                participantId = it.pId,
                                checkedInAt = nowIso,
                                status = "present",
                                participantName = it.name,
                                participantUsn = it.pId,
                                teamId = it.teamId,
                                teamName = it.teamName,
                                isPendingSync = false
                            )
                        }

                        attendanceRepository.recordBulkScanSuccess(
                            eventId = eventIdInt,
                            markedAttendees = markedAttendees,
                            progress = response.progress,
                            newAttendance = response.eventAttendance,
                            total = response.totalParticipants
                        )

                        // Immediate optimistic updates to attendance matrix
                        (markedIds + duplicateIds).forEach { pId ->
                            attendanceMatrixRepository.addLocalScan(eventIdInt, pId)
                        }

                        val duplicateNames = currentPending.filter { it.pId in duplicateIds }.map { it.name }
                            .ifEmpty { response.duplicates }

                        _bulkSummary.value = BulkSummary(
                            markedCount = response.markedPresent.size,
                            duplicateNames = duplicateNames
                        )
                        _pendingList.value = emptyList()
                        hapticFeedbackUtil.vibrateSuccess()
                        audioFeedbackUtil.playBeepSuccess()
                    }
                }.onFailure { throwable ->
                    // Network or DB failure: keep pending list intact so user can retry
                    hapticFeedbackUtil.vibrateError()
                    audioFeedbackUtil.playBeepError()
                    val isNetwork = throwable is java.io.IOException ||
                        throwable is java.net.SocketException ||
                        throwable is java.net.UnknownHostException
                    if (isNetwork) {
                        showFeedback(ScanResult.NetworkError)
                    } else {
                        val msg = sanitizeErrorMessage(throwable.message, "Bulk submission failed")
                        showFeedback(ScanResult.Error(msg))
                    }
                }
            } finally {
                _isSubmitting.value = false
                scanMutex.unlock()
            }
        }
    }

    private fun sanitizeErrorMessage(raw: String?, fallback: String): String {
        if (raw.isNullOrBlank()) return fallback
        val cleaned = if (raw.contains("URL:", ignoreCase = true)) {
            raw.substringBefore("URL:").trim()
        } else {
            raw.trim()
        }
        return cleaned.ifBlank { fallback }
    }

    private fun showFeedback(result: ScanResult) {
        _scanResult.value = result
        dismissBannerJob?.cancel()
        dismissBannerJob = viewModelScope.launch {
            delay(2500)
            _scanResult.value = null
        }
    }

    private fun showBulkHint(hint: String) {
        _bulkHint.value = hint
        dismissHintJob?.cancel()
        dismissHintJob = viewModelScope.launch {
            delay(2000)
            _bulkHint.value = null
        }
    }

    fun toggleTorch() {
        _isTorchOn.value = !_isTorchOn.value
    }

    fun switchCamera() {
        _isBackCamera.value = !_isBackCamera.value
    }
}
