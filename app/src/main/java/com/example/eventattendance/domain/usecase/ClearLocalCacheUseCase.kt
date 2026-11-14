package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.repository.AttendanceMatrixRepository
import com.example.eventattendance.domain.repository.AttendanceRepository
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import javax.inject.Inject

class ClearLocalCacheUseCase @Inject constructor(
    private val participantCacheRepository: ParticipantCacheRepository,
    private val attendanceRepository: AttendanceRepository,
    private val attendanceMatrixRepository: AttendanceMatrixRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return runCatching {
            // 1. Purge all local cache files and clear in-memory state
            participantCacheRepository.clearCache()
            attendanceRepository.clearCache()
            attendanceMatrixRepository.clearMatrix()

            // 2. Fetch fresh live state from database (get_all_participants & sync_all_events_attendance)
            participantCacheRepository.refreshParticipants()
            attendanceRepository.syncAllEventsAttendance(forcePull = true)
            attendanceMatrixRepository.refresh()
            Unit
        }
    }
}
