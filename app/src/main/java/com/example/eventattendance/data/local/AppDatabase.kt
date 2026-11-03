package com.example.eventattendance.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.eventattendance.data.local.dao.AttendanceDao
import com.example.eventattendance.data.local.dao.EligibilityDao
import com.example.eventattendance.data.local.dao.EventDao
import com.example.eventattendance.data.local.dao.ParticipantDao
import com.example.eventattendance.data.local.dao.PendingScanDao
import com.example.eventattendance.data.local.dao.SyncMetaDao
import com.example.eventattendance.data.local.entity.AttendanceRecordEntity
import com.example.eventattendance.data.local.entity.EventEligibilityEntity
import com.example.eventattendance.data.local.entity.EventEntity
import com.example.eventattendance.data.local.entity.ParticipantEntity
import com.example.eventattendance.data.local.entity.PendingScanEntity
import com.example.eventattendance.data.local.entity.SyncMetaEntity

@Database(
    entities = [
        EventEntity::class,
        ParticipantEntity::class,
        EventEligibilityEntity::class,
        AttendanceRecordEntity::class,
        PendingScanEntity::class,
        SyncMetaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun participantDao(): ParticipantDao
    abstract fun eligibilityDao(): EligibilityDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun pendingScanDao(): PendingScanDao
    abstract fun syncMetaDao(): SyncMetaDao
}
