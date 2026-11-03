package com.example.eventattendance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.eventattendance.data.local.entity.PendingScanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingScanDao {

    @Query("SELECT * FROM pending_scans WHERE syncStatus = 'PENDING' ORDER BY scannedAt ASC")
    fun getPendingScansFlow(): Flow<List<PendingScanEntity>>

    @Query("SELECT * FROM pending_scans WHERE syncStatus = 'PENDING' ORDER BY scannedAt ASC")
    suspend fun getPendingScans(): List<PendingScanEntity>

    @Query("SELECT * FROM pending_scans WHERE eventId = :eventId AND syncStatus = 'PENDING'")
    fun getPendingScansForEventFlow(eventId: String): Flow<List<PendingScanEntity>>

    @Query("SELECT COUNT(*) FROM pending_scans WHERE eventId = :eventId AND syncStatus = 'PENDING'")
    fun getPendingCountForEventFlow(eventId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM pending_scans WHERE syncStatus = 'PENDING'")
    fun getTotalPendingCountFlow(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM pending_scans WHERE eventId = :eventId AND participantId = :participantId)")
    suspend fun isPendingForEvent(eventId: String, participantId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPendingScan(scan: PendingScanEntity): Long

    @Update
    suspend fun updatePendingScans(scans: List<PendingScanEntity>)

    @Query("UPDATE pending_scans SET syncStatus = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("DELETE FROM pending_scans WHERE syncStatus = 'SYNCED'")
    suspend fun deleteSyncedScans()
}
