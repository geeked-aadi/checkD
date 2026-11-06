package com.example.eventattendance.domain.usecase

import com.example.eventattendance.domain.model.AdminDirectoryData
import com.example.eventattendance.domain.repository.DirectoryRepository
import javax.inject.Inject

class GetAdminDirectoryUseCase @Inject constructor(
    private val directoryRepository: DirectoryRepository
) {
    suspend operator fun invoke(): Result<AdminDirectoryData> {
        return directoryRepository.getAdminDirectoryData()
    }
}
