package com.example.eventattendance.domain.repository

import com.example.eventattendance.domain.model.AttendanceMatrix
import kotlinx.coroutines.flow.StateFlow

interface AttendanceMatrixRepository {
    val matrixFlow: StateFlow<AttendanceMatrix?>
    val lastUpdated: StateFlow<Long?>
    suspend fun refresh(): Result<Unit>
    fun addLocalScan(eventId: Int, pId: String)
}
