package com.example.eventattendance.presentation.verifier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.model.CachedParticipant
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import com.example.eventattendance.domain.usecase.VerifyParticipantUseCase
import com.example.eventattendance.util.AudioFeedbackUtil
import com.example.eventattendance.util.HapticFeedbackUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface VerifierState {
    data object Idle : VerifierState
    data class Valid(val participant: CachedParticipant) : VerifierState
    data class Invalid(val scannedToken: String) : VerifierState
}

@HiltViewModel
class QrVerifierViewModel @Inject constructor(
    private val verifyParticipantUseCase: VerifyParticipantUseCase,
    private val participantCacheRepository: ParticipantCacheRepository,
    private val hapticFeedbackUtil: HapticFeedbackUtil,
    private val audioFeedbackUtil: AudioFeedbackUtil
) : ViewModel() {

    private val _verifierState = MutableStateFlow<VerifierState>(VerifierState.Idle)
    val verifierState: StateFlow<VerifierState> = _verifierState.asStateFlow()

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _isBackCamera = MutableStateFlow(true)
    val isBackCamera: StateFlow<Boolean> = _isBackCamera.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val totalCachedCount: StateFlow<Int> = participantCacheRepository.participantsFlow
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private var lastScannedToken: String? = null
    private var lastScanTimestamp: Long = 0
    private var autoDismissJob: Job? = null

    private val debounceSameTokenMs = 1500L
    private val debounceGeneralMs = 500L

    init {
        // Ensure participants cache is initialized and loaded on launch
        viewModelScope.launch {
            if (participantCacheRepository.isCacheEmpty()) {
                refreshRoster()
            }
        }
    }

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

        viewModelScope.launch {
            val participant = verifyParticipantUseCase(trimmedToken)
            if (participant != null) {
                hapticFeedbackUtil.vibrateSuccess()
                audioFeedbackUtil.playBeepSuccess()
                _verifierState.value = VerifierState.Valid(participant)
            } else {
                hapticFeedbackUtil.vibrateError()
                audioFeedbackUtil.playBeepError()
                _verifierState.value = VerifierState.Invalid(trimmedToken)
            }

            // Auto-dismiss feedback after 4 seconds
            autoDismissJob?.cancel()
            autoDismissJob = viewModelScope.launch {
                delay(4000)
                _verifierState.value = VerifierState.Idle
            }
        }
    }

    fun dismissResult() {
        autoDismissJob?.cancel()
        _verifierState.value = VerifierState.Idle
    }

    fun toggleTorch() {
        _isTorchOn.value = !_isTorchOn.value
    }

    fun switchCamera() {
        _isBackCamera.value = !_isBackCamera.value
    }

    fun refreshRoster() {
        viewModelScope.launch {
            _isRefreshing.value = true
            participantCacheRepository.refreshParticipants()
            _isRefreshing.value = false
        }
    }
}
