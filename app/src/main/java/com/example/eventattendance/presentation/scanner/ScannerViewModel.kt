package com.example.eventattendance.presentation.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.model.ScanResult
import com.example.eventattendance.domain.usecase.ProcessScanUseCase
import com.example.eventattendance.util.AudioFeedbackUtil
import com.example.eventattendance.util.HapticFeedbackUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val processScanUseCase: ProcessScanUseCase,
    private val hapticFeedbackUtil: HapticFeedbackUtil,
    private val audioFeedbackUtil: AudioFeedbackUtil,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val eventId: String = checkNotNull(savedStateHandle["eventId"])

    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _isBackCamera = MutableStateFlow(true)
    val isBackCamera: StateFlow<Boolean> = _isBackCamera.asStateFlow()

    private var lastScannedToken: String? = null
    private var lastScanTimestamp: Long = 0
    private var dismissBannerJob: Job? = null

    // 1000ms cooldown for the exact same token, 500ms between different tokens
    private val debounceSameTokenMs = 1200L
    private val debounceGeneralMs = 500L

    fun onQrCodeScanned(qrToken: String) {
        val now = System.currentTimeMillis()

        if (qrToken == lastScannedToken && (now - lastScanTimestamp) < debounceSameTokenMs) {
            return
        }

        if ((now - lastScanTimestamp) < debounceGeneralMs) {
            return
        }

        lastScannedToken = qrToken
        lastScanTimestamp = now

        viewModelScope.launch {
            val result = processScanUseCase(eventId, qrToken)
            _scanResult.value = result

            // Haptic & Audio Feedback
            when (result) {
                is ScanResult.Success -> {
                    hapticFeedbackUtil.vibrateSuccess()
                    audioFeedbackUtil.playBeepSuccess()
                }
                is ScanResult.AlreadyCheckedIn -> {
                    hapticFeedbackUtil.vibrateError()
                    audioFeedbackUtil.playBeepError()
                }
                is ScanResult.NotEligible,
                is ScanResult.InvalidQr,
                is ScanResult.EventClosed,
                is ScanResult.Error -> {
                    hapticFeedbackUtil.vibrateError()
                    audioFeedbackUtil.playBeepError()
                }
            }

            // Auto-hide feedback after 2.5 seconds
            dismissBannerJob?.cancel()
            dismissBannerJob = launch {
                delay(2500)
                _scanResult.value = null
            }
        }
    }

    fun toggleTorch() {
        _isTorchOn.value = !_isTorchOn.value
    }

    fun switchCamera() {
        _isBackCamera.value = !_isBackCamera.value
    }
}
