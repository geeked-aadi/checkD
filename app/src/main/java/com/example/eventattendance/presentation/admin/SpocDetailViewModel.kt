package com.example.eventattendance.presentation.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.model.AttendanceMatrix
import com.example.eventattendance.domain.model.SpocDashboardData
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository
import com.example.eventattendance.domain.repository.AuthRepository
import com.example.eventattendance.domain.repository.DirectoryRepository
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpocDetailViewModel @Inject constructor(
    private val directoryRepository: DirectoryRepository,
    private val participantCacheRepository: ParticipantCacheRepository,
    private val attendanceMatrixRepository: AttendanceMatrixRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val spocId: String = checkNotNull(savedStateHandle["spocId"])

    val currentRole: StateFlow<UserRole> = authRepository.currentRole

    private val _spocData = MutableStateFlow<SpocDashboardData?>(null)
    val spocData: StateFlow<SpocDashboardData?> = _spocData.asStateFlow()

    val matrix: StateFlow<AttendanceMatrix?> = attendanceMatrixRepository.matrixFlow
    val lastUpdated: StateFlow<Long?> = attendanceMatrixRepository.lastUpdated

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            directoryRepository.getSpocDashboardData(spocId)
                .onSuccess { _spocData.value = it }
            attendanceMatrixRepository.refresh()
            _isRefreshing.value = false
        }
    }
}
