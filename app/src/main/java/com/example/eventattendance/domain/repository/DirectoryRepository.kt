package com.example.eventattendance.domain.repository

import com.example.eventattendance.domain.model.AdminDirectoryData
import com.example.eventattendance.domain.model.SpocDashboardData

interface DirectoryRepository {
    suspend fun getSpocDashboardData(spocId: String): Result<SpocDashboardData>
    suspend fun getAdminDirectoryData(): Result<AdminDirectoryData>
}
