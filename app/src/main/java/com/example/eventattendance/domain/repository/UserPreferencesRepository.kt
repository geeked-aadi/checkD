package com.example.eventattendance.domain.repository

import com.example.eventattendance.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)

    val isHapticEnabled: Flow<Boolean>
    suspend fun setHapticEnabled(enabled: Boolean)

    val isSoundEnabled: Flow<Boolean>
    suspend fun setSoundEnabled(enabled: Boolean)
}
