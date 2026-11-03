package com.example.eventattendance.domain.repository

import com.example.eventattendance.domain.model.AttendanceRecord
import com.example.eventattendance.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun getAttendanceForEventFlow(eventId: String): Flow<List<AttendanceRecord>>
    fun getScannedCountFlow(eventId: String): Flow<Int>
    suspend fun processScan(eventId: String, qrToken: String): ScanResult
    suspend fun exportAttendanceCsv(eventId: String): Result<String>
}
