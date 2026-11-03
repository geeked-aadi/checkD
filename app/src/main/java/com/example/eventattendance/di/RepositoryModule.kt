package com.example.eventattendance.di

import com.example.eventattendance.data.repository.AttendanceRepositoryImpl
import com.example.eventattendance.data.repository.AuthRepositoryImpl
import com.example.eventattendance.data.repository.EventRepositoryImpl
import com.example.eventattendance.data.repository.ParticipantRepositoryImpl
import com.example.eventattendance.data.repository.SyncRepositoryImpl
import com.example.eventattendance.domain.repository.AttendanceRepository
import com.example.eventattendance.domain.repository.AuthRepository
import com.example.eventattendance.domain.repository.EventRepository
import com.example.eventattendance.domain.repository.ParticipantRepository
import com.example.eventattendance.domain.repository.SyncRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository

    @Binds
    @Singleton
    abstract fun bindParticipantRepository(impl: ParticipantRepositoryImpl): ParticipantRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository
}
