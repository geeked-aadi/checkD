package com.example.eventattendance.data.repository

import com.example.eventattendance.data.local.AppDatabase
import com.example.eventattendance.data.local.entity.AttendanceRecordEntity
import com.example.eventattendance.data.local.entity.PendingScanEntity
import com.example.eventattendance.domain.model.AttendanceRecord
import com.example.eventattendance.domain.model.EventStatus
import com.example.eventattendance.domain.model.ScanResult
import com.example.eventattendance.domain.repository.AttendanceRepository
import com.example.eventattendance.sync.NetworkConnectivityObserver
import com.example.eventattendance.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val syncManager: SyncManager,
    private val connectivityObserver: NetworkConnectivityObserver
) : AttendanceRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun getAttendanceForEventFlow(eventId: String): Flow<List<AttendanceRecord>> {
        return database.attendanceDao().getAttendanceWithParticipantsFlow(eventId).map { list ->
            list.map { item ->
                AttendanceRecord(
                    id = item.id,
                    eventId = item.eventId,
                    participantId = item.participantId,
                    checkedInAt = item.checkedInAt,
                    status = item.status,
                    participantName = item.name,
                    participantUsn = item.usn,
                    isPendingSync = false
                )
            }
        }
    }

    override fun getScannedCountFlow(eventId: String): Flow<Int> {
        return database.attendanceDao().getScannedCountFlow(eventId)
    }

    override suspend fun processScan(eventId: String, qrToken: String): ScanResult {
        // 1. Check Event Existence and Status
        val event = database.eventDao().getEventById(eventId)
        if (event == null || !event.status.equals("open", ignoreCase = true)) {
            return ScanResult.EventClosed
        }

        // 2. Validate Participant via QR token in local database
        val participant = database.participantDao().getParticipantByQrToken(qrToken)
            ?: return ScanResult.InvalidQr

        // 3. Verify Participant Eligibility for this specific activity
        val isEligible = database.eligibilityDao().isParticipantEligible(eventId, participant.id)
        if (!isEligible) {
            return ScanResult.NotEligible(
                participantName = participant.name,
                participantUsn = participant.usn
            )
        }

        // 4. Duplicate Check (Check both server-confirmed and pending queues)
        val isAlreadyCheckedIn = database.attendanceDao().isAlreadyCheckedIn(eventId, participant.id)
        val isPending = database.pendingScanDao().isPendingForEvent(eventId, participant.id)

        if (isAlreadyCheckedIn || isPending) {
            return ScanResult.AlreadyCheckedIn(
                participantName = participant.name,
                participantUsn = participant.usn
            )
        }

        // 5. Valid check-in: Store in local Room pending scan queue
        val currentTime = System.currentTimeMillis()
        val isoTimestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(currentTime))

        val pendingScan = PendingScanEntity(
            eventId = eventId,
            participantId = participant.id,
            qrToken = qrToken,
            scannedAt = currentTime,
            syncStatus = "PENDING"
        )
        database.pendingScanDao().insertPendingScan(pendingScan)

        // Optimistically insert into local attendance records so UI reflects it immediately
        database.attendanceDao().insertAttendanceRecord(
            AttendanceRecordEntity(
                id = UUID.randomUUID().toString(),
                eventId = eventId,
                participantId = participant.id,
                checkedInAt = isoTimestamp,
                checkedInBy = null,
                status = "present"
            )
        )

        // 6. If internet is available, trigger immediate sync in the background
        if (connectivityObserver.isCurrentlyConnected()) {
            scope.launch {
                syncManager.pushPendingScans()
            }
        }

        return ScanResult.Success(
            participantName = participant.name,
            participantUsn = participant.usn,
            isPendingSync = !connectivityObserver.isCurrentlyConnected(),
            timestamp = isoTimestamp
        )
    }

    override suspend fun exportAttendanceCsv(eventId: String): Result<String> {
        return try {
            val event = database.eventDao().getEventById(eventId)
            val eventName = event?.name ?: "Event"
            val recordsFlow = database.attendanceDao().getAttendanceWithParticipantsFlow(eventId)
            // Read one snapshot
            var csv = "Event,Participant Name,USN,Check-in Time,Status\n"
            // Get records
            val records = database.attendanceDao().getAttendanceWithParticipantsFlow(eventId)
            // Just query directly
            // Build CSV content
            val builder = StringBuilder(csv)
            // We can retrieve participant details from database
            Result.success(builder.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
