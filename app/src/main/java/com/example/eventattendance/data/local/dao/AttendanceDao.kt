package com.example.eventattendance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.eventattendance.data.local.entity.AttendanceRecordEntity
import kotlinx.coroutines.flow.Flow

data class AttendanceWithParticipant(
    val id: String,
    val eventId: String,
    val participantId: String,
    val checkedInAt: String,
    val status: String,
    val name: String,
    val usn: String
)

@Dao
interface AttendanceDao {

    @Query("SELECT COUNT(*) FROM attendance_records WHERE eventId = :eventId")
    fun getScannedCountFlow(eventId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE eventId = :eventId")
    suspend fun getScannedCount(eventId: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM attendance_records WHERE eventId = :eventId AND participantId = :participantId)")
    suspend fun isAlreadyCheckedIn(eventId: String, participantId: String): Boolean

    @Transaction
    @Query("""
        SELECT a.id, a.eventId, a.participantId, a.checkedInAt, a.status, p.name, p.usn
        FROM attendance_records a
        INNER JOIN participants p ON a.participantId = p.id
        WHERE a.eventId = :eventId
        ORDER BY a.checkedInAt DESC
    """)
    fun getAttendanceWithParticipantsFlow(eventId: String): Flow<List<AttendanceWithParticipant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecords(records: List<AttendanceRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: AttendanceRecordEntity)

    @Query("DELETE FROM attendance_records")
    suspend fun clearAll()
}
