package com.example.eventattendance.presentation.spoc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.calculator.ProgressCalculator
import com.example.eventattendance.domain.model.AttendanceMatrix
import com.example.eventattendance.domain.model.ProgressResult
import com.example.eventattendance.domain.model.SpocDashboardData
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository
import com.example.eventattendance.domain.repository.AuthRepository
import com.example.eventattendance.domain.repository.DirectoryRepository
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import com.example.eventattendance.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpocEventCardData(
    val eventId: Int,
    val eventName: String,
    val startsFrom: String?,
    val endsOn: String?,
    val progress: ProgressResult
)

@HiltViewModel
class SpocDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val directoryRepository: DirectoryRepository,
    private val participantCacheRepository: ParticipantCacheRepository,
    private val attendanceMatrixRepository: AttendanceMatrixRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    val currentRole: StateFlow<UserRole> = authRepository.currentRole

    private val _selectedTab = MutableStateFlow(0) // 0: Events, 1: Team Progress, 2: Profile
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _spocData = MutableStateFlow<SpocDashboardData?>(null)
    val spocData: StateFlow<SpocDashboardData?> = _spocData.asStateFlow()

    val matrix: StateFlow<AttendanceMatrix?> = attendanceMatrixRepository.matrixFlow
    val lastUpdated: StateFlow<Long?> = attendanceMatrixRepository.lastUpdated

    // SPOC's member IDs set
    val spocMemberPIds: StateFlow<Set<String>> = combine(
        currentRole,
        participantCacheRepository.participantsFlow
    ) { role, _ ->
        val spocId = (role as? UserRole.Spoc)?.spocId
        if (!spocId.isNullOrBlank()) {
            participantCacheRepository.getMemberPIds(spocId)
        } else {
            emptySet()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    // Events list computed for THIS SPOC only
    val spocEvents: StateFlow<List<SpocEventCardData>> = combine(
        matrix,
        spocMemberPIds
    ) { matrixData, memberPIds ->
        val events = matrixData?.events ?: emptyList()
        events.map { event ->
            val progress = ProgressCalculator.eventProgress(memberPIds, event.presentPIds)
            SpocEventCardData(
                eventId = event.eventId,
                eventName = event.eventName,
                startsFrom = event.startsFrom,
                endsOn = event.endsOn,
                progress = progress
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        refresh()
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val role = authRepository.currentRole.value
            val spocId = (role as? UserRole.Spoc)?.spocId

            var networkFailed = false

            // 1. Refresh participant cache
            participantCacheRepository.refreshParticipants().onFailure { networkFailed = true }

            // 2. Refresh SPOC directory data
            if (!spocId.isNullOrBlank()) {
                directoryRepository.getSpocDashboardData(spocId)
                    .onSuccess { _spocData.value = it }
                    .onFailure { networkFailed = true }
            }

            // 3. Refresh attendance matrix
            attendanceMatrixRepository.refresh().onFailure { networkFailed = true }

            _isOffline.value = networkFailed
            _isRefreshing.value = false
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onComplete()
        }
    }
}
