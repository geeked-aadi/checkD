package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.model.ScanResult
import com.example.eventattendance.domain.repository.AttendanceRepository
import javax.inject.Inject

class ProcessScanUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(eventId: String, qrToken: String): ScanResult {
        return attendanceRepository.processScan(eventId, qrToken)
    }
}
