package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.model.SpocDashboardData
import com.example.eventattendance.domain.repository.DirectoryRepository
import javax.inject.Inject

class GetSpocDashboardUseCase @Inject constructor(
    private val directoryRepository: DirectoryRepository
) {
    suspend operator fun invoke(spocId: String): Result<SpocDashboardData> {
        return directoryRepository.getSpocDashboardData(spocId)
    }
}
