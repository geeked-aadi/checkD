package com.example.eventattendance.domain.usecase

import com.example.eventattendance.data.remote.dto.MarkAttendanceResponseDto
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.domain.repository.AttendanceRepository
import com.example.eventattendance.domain.repository.AuthRepository
import javax.inject.Inject

class MarkAttendanceUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(eventId: Int, pIds: List<String>): Result<MarkAttendanceResponseDto> {
        val role = when (authRepository.currentRole.value) {
            is UserRole.Volunteer -> "vol"
            is UserRole.Spoc -> "spoc"
            is UserRole.Admin -> "admin"
        }
        return attendanceRepository.markAttendance(eventId, pIds, role)
    }
}
