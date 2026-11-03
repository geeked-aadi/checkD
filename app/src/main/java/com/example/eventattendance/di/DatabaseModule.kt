package com.example.eventattendance.di

import android.content.Context
import androidx.room.Room
import com.example.eventattendance.data.local.AppDatabase
import com.example.eventattendance.data.local.dao.AttendanceDao
import com.example.eventattendance.data.local.dao.EligibilityDao
import com.example.eventattendance.data.local.dao.EventDao
import com.example.eventattendance.data.local.dao.ParticipantDao
import com.example.eventattendance.data.local.dao.PendingScanDao
import com.example.eventattendance.data.local.dao.SyncMetaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "checkd_attendance.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideEventDao(database: AppDatabase): EventDao = database.eventDao()

    @Provides
    fun provideParticipantDao(database: AppDatabase): ParticipantDao = database.participantDao()

    @Provides
    fun provideEligibilityDao(database: AppDatabase): EligibilityDao = database.eligibilityDao()

    @Provides
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao = database.attendanceDao()

    @Provides
    fun providePendingScanDao(database: AppDatabase): PendingScanDao = database.pendingScanDao()

    @Provides
    fun provideSyncMetaDao(database: AppDatabase): SyncMetaDao = database.syncMetaDao()
}
