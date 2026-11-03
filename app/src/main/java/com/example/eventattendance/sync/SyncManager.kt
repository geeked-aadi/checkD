package com.example.eventattendance.sync

import androidx.room.withTransaction
import com.example.eventattendance.data.local.AppDatabase
import com.example.eventattendance.data.local.entity.AttendanceRecordEntity
import com.example.eventattendance.data.local.entity.SyncMetaEntity
import com.example.eventattendance.data.remote.datasource.AttendanceRemoteDataSource
import com.example.eventattendance.data.remote.datasource.EventRemoteDataSource
import com.example.eventattendance.data.remote.datasource.ParticipantRemoteDataSource
import com.example.eventattendance.data.remote.datasource.SyncRemoteDataSource
import com.example.eventattendance.domain.model.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val database: AppDatabase,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val participantRemoteDataSource: ParticipantRemoteDataSource,
    private val attendanceRemoteDataSource: AttendanceRemoteDataSource,
    private val syncRemoteDataSource: SyncRemoteDataSource,
    private val connectivityObserver: NetworkConnectivityObserver
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Observe connectivity changes to auto-push pending scans when network returns
        scope.launch {
            connectivityObserver.observe().collect { status ->
                if (status == ConnectivityObserver.Status.Available) {
                    syncAll()
                }
            }
        }
    }

    suspend fun syncAll(forcePull: Boolean = false): Result<Unit> {
        if (!connectivityObserver.isCurrentlyConnected()) {
            return Result.failure(Exception("Offline: network unavailable"))
        }

        _syncState.value = SyncState.Syncing

        return try {
            // Step 1: Pull cloud changes if database was modified
            pullCloudChanges(forcePull)

            // Step 2: Push local offline scans to cloud
            pushPendingScans()

            _syncState.value = SyncState.Success("Synced with cloud")
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e.message ?: "Sync failed")
            Result.failure(e)
        }
    }

    /**
     * Checks if the cloud database changed.
     * If modified, clears local mirror tables and repopulates them from cloud.
     * Note: pending_scans are NEVER wiped!
     */
    suspend fun pullCloudChanges(force: Boolean = false): Boolean {
        if (!connectivityObserver.isCurrentlyConnected()) return false

        val cloudMeta = syncRemoteDataSource.getSyncMetadata() ?: return false
        val localMeta = database.syncMetaDao().getSyncMeta()

        val hasChanged = force || localMeta == null || localMeta.cloudTimestamp != cloudMeta.lastModifiedAt

        if (!hasChanged) {
            return false // Local cache is up-to-date
        }

        // Fetch fresh snapshots from cloud
        val events = eventRemoteDataSource.getAllEvents()
        val participants = participantRemoteDataSource.getAllParticipants()
        val eligibility = participantRemoteDataSource.getAllEligibility()
        val attendance = attendanceRemoteDataSource.getAllAttendanceRecords()

        // Replace local cached database inside a transaction
        database.withTransaction {
            database.eventDao().clearAll()
            database.participantDao().clearAll()
            database.eligibilityDao().clearAll()
            database.attendanceDao().clearAll()

            database.eventDao().insertEvents(events.map { it.toEntity() })
            database.participantDao().insertParticipants(participants.map { it.toEntity() })
            database.eligibilityDao().insertEligibility(eligibility.map { it.toEntity() })
            database.attendanceDao().insertAttendanceRecords(attendance.map { it.toEntity() })

            database.syncMetaDao().saveSyncMeta(
                SyncMetaEntity(
                    key = "last_sync",
                    cloudTimestamp = cloudMeta.lastModifiedAt,
                    localSyncedAt = System.currentTimeMillis()
                )
            )
        }

        return true
    }

    /**
     * Pushes pending offline scans to Supabase using the mark_bulk_attendance RPC.
     */
    suspend fun pushPendingScans(): Int {
        if (!connectivityObserver.isCurrentlyConnected()) return 0

        val pendingScans = database.pendingScanDao().getPendingScans()
        if (pendingScans.isEmpty()) return 0

        var totalSynced = 0

        // Group scans by eventId for bulk RPC submission
        val groupedByEvent = pendingScans.groupBy { it.eventId }

        for ((eventId, scans) in groupedByEvent) {
            val participantIds = scans.map { it.participantId }

            try {
                val response = attendanceRemoteDataSource.markBulkAttendance(eventId, participantIds)
                if (response.success) {
                    val scanIds = scans.map { it.id }
                    database.pendingScanDao().markAsSynced(scanIds)

                    // Insert into local attendance records so UI reflects server-confirmation
                    val recordsToInsert = scans.map { scan ->
                        AttendanceRecordEntity(
                            id = UUID.randomUUID().toString(),
                            eventId = scan.eventId,
                            participantId = scan.participantId,
                            checkedInAt = java.text.SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                                java.util.Locale.US
                            ).format(java.util.Date(scan.scannedAt)),
                            checkedInBy = null,
                            status = "present"
                        )
                    }
                    database.attendanceDao().insertAttendanceRecords(recordsToInsert)

                    totalSynced += response.inserted
                }
            } catch (e: Exception) {
                // Keep scans as PENDING for the next retry attempt
            }
        }

        // Clean up confirmed synced rows from the queue
        database.pendingScanDao().deleteSyncedScans()

        return totalSynced
    }
}
