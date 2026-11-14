package com.example.eventattendance.domain.repository

import com.example.eventattendance.domain.model.AttendanceMatrix
import com.example.eventattendance.domain.model.EventAttendance
import kotlinx.coroutines.flow.StateFlow

interface AttendanceMatrixRepository {
    val matrixFlow: StateFlow<AttendanceMatrix?>
    val lastUpdated: StateFlow<Long?>
    suspend fun refresh(): Result<Unit>
    fun addLocalScan(eventId: Int, pId: String)
    fun updateLocalEvent(eventId: Int, name: String?, startsFrom: String, endsOn: String)
    fun addLocalEvent(event: EventAttendance)
    fun clearMatrix()
}

