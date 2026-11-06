package com.example.eventattendance.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.calculator.ProgressCalculator
import com.example.eventattendance.domain.model.AdminDirectoryData
import com.example.eventattendance.domain.model.AttendanceMatrix
import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.domain.model.EventStatus
import com.example.eventattendance.domain.model.EventWithStats
import com.example.eventattendance.domain.model.OverallResult
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository
import com.example.eventattendance.domain.repository.AttendanceRepository
import com.example.eventattendance.domain.repository.AuthRepository
import com.example.eventattendance.domain.repository.DirectoryRepository
import com.example.eventattendance.domain.repository.EventRepository
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import com.example.eventattendance.domain.usecase.LogoutUseCase
import com.example.eventattendance.domain.usecase.ManageEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val attendanceMatrixRepository: AttendanceMatrixRepository,
    private val participantCacheRepository: ParticipantCacheRepository,
    private val directoryRepository: DirectoryRepository,
    private val attendanceRepository: AttendanceRepository,
    private val eventRepository: EventRepository,
    private val manageEventsUseCase: ManageEventsUseCase,
    private val authRepository: AuthRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    val currentRole: StateFlow<UserRole> = authRepository.currentRole

    private val _selectedTab = MutableStateFlow(0) // 0: Overview, 1: Events, 2: Directory, 3: SPOCs, 4: Settings
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    val matrix: StateFlow<AttendanceMatrix?> = attendanceMatrixRepository.matrixFlow
    val lastUpdated: StateFlow<Long?> = attendanceMatrixRepository.lastUpdated

    private val _directoryData = MutableStateFlow<AdminDirectoryData?>(null)
    val directoryData: StateFlow<AdminDirectoryData?> = _directoryData.asStateFlow()

    val eventsWithStats: StateFlow<List<EventWithStats>> = attendanceRepository.eventsWithStatsFlow

private data class SpocFilterConfig(
    val eventId: Int? = null,
    val sort: SpocAnalyticsSort = SpocAnalyticsSort.LOWEST
)

    // SPOC Analytics Filter & Sort state
    private val _spocFilter = MutableStateFlow(SpocFilterConfig())

    // Directory Search & Filters
    private val _directorySearch = MutableStateFlow("")
    val directorySearch: StateFlow<String> = _directorySearch.asStateFlow()

    private val _filterSpocId = MutableStateFlow<String?>(null)
    val filterSpocId: StateFlow<String?> = _filterSpocId.asStateFlow()

    private val _filterTeamId = MutableStateFlow<String?>(null)
    val filterTeamId: StateFlow<String?> = _filterTeamId.asStateFlow()

    private val _actionMessage = MutableSharedFlow<String>()
    val actionMessage: SharedFlow<String> = _actionMessage.asSharedFlow()

    // Overall Overview Stats
    val overallResult: StateFlow<OverallResult> = combine(
        matrix,
        participantCacheRepository.participantsFlow
    ) { matrixData, cache ->
        val allIds = cache.keys
        val events = matrixData?.events ?: emptyList()
        ProgressCalculator.overallProgress(allIds, events)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OverallResult(percent = null, presentSum = 0, expectedSum = 0, startedEventCount = 0)
    )

    // SPOC Analytics computation with single-pass participant aggregation
    val spocAnalyticsUiState: StateFlow<SpocAnalyticsUiState> = combine(
        directoryData,
        matrix,
        participantCacheRepository.participantsFlow,
        _spocFilter
    ) { directory, matrixData, participantsMap, filter ->
        val selectedEventId = filter.eventId
        val sort = filter.sort
        val spocs = directory?.spocs ?: emptyList()
        val events = matrixData?.events ?: emptyList()

        // 1. Single pass aggregation over participants: group by spocId and teamId
        val spocToMemberIds = mutableMapOf<String, MutableSet<String>>()
        val teamToMemberIds = mutableMapOf<String, MutableSet<String>>()

        for (participant in participantsMap.values) {
            val sId = participant.spocId
            if (!sId.isNullOrBlank()) {
                spocToMemberIds.getOrPut(sId) { mutableSetOf() }.add(participant.pId)
            }
            val tId = participant.teamId
            if (!tId.isNullOrBlank()) {
                teamToMemberIds.getOrPut(tId) { mutableSetOf() }.add(participant.pId)
            }
        }

        val spocList = spocs.map { spocWithTeams ->
            val spoc = spocWithTeams.spoc
            val spocId = spoc.spocId
            val teamCount = spocWithTeams.teams.size

            // Union of member IDs from cache index and directory team list
            val memberIdsFromCache = spocToMemberIds[spocId] ?: emptySet()
            val memberIdsFromTeams = spocWithTeams.teams.flatMap { team ->
                teamToMemberIds[team.teamId] ?: emptySet()
            }.toSet()
            val memberIds = memberIdsFromCache + memberIdsFromTeams
            val studentCount = memberIds.size

            // Calculate perEventPct: eventId -> percentage (0..100)
            val perEventPct = mutableMapOf<Int, Int>()
            for (event in events) {
                val presentInEvt = if (studentCount > 0) memberIds.count { it in event.presentPIds } else 0
                val evPct = if (studentCount > 0) {
                    ((presentInEvt * 100) / studentCount).coerceIn(0, 100)
                } else {
                    0
                }
                perEventPct[event.eventId] = evPct
            }

            // Calculate checked, total, pct, pending based on selectedEventId
            val checked: Int
            val total: Int
            val pct: Float?
            val pending: Int

            if (selectedEventId == null) {
                // "All events" = completion across all events, not only Live
                val eventCount = events.size
                total = studentCount * eventCount
                checked = if (studentCount > 0) {
                    events.sumOf { event -> memberIds.count { it in event.presentPIds } }
                } else {
                    0
                }
                pending = if (total > 0) (total - checked).coerceAtLeast(0) else 0
                pct = if (total > 0) (checked.toFloat() / total.toFloat()).coerceIn(0f, 1f) else null
            } else {
                // Single selected event
                val targetEvent = events.find { it.eventId == selectedEventId }
                total = studentCount
                checked = if (targetEvent != null && studentCount > 0) {
                    memberIds.count { it in targetEvent.presentPIds }
                } else {
                    0
                }
                pending = if (total > 0) (total - checked).coerceAtLeast(0) else 0
                pct = if (total > 0) (checked.toFloat() / total.toFloat()).coerceIn(0f, 1f) else null
            }

            SpocAnalytics(
                spocId = spocId,
                name = spoc.name,
                phone = spoc.phoneNumber,
                teamCount = teamCount,
                studentCount = studentCount,
                checked = checked,
                total = total,
                pct = pct,
                pending = pending,
                perEventPct = perEventPct
            )
        }

        // Sort SPOCs
        val sortedSpocs = when (sort) {
            SpocAnalyticsSort.LOWEST -> spocList.sortedWith(
                compareBy<SpocAnalytics> { it.pct == null }
                    .thenBy { it.pct ?: 0f }
                    .thenByDescending { it.pending }
                    .thenBy { it.name.lowercase() }
            )
            SpocAnalyticsSort.HIGHEST -> spocList.sortedWith(
                compareBy<SpocAnalytics> { it.pct == null }
                    .thenByDescending { it.pct ?: 0f }
                    .thenBy { it.pending }
                    .thenBy { it.name.lowercase() }
            )
            SpocAnalyticsSort.NAME -> spocList.sortedBy { it.name.lowercase() }
        }

        SpocAnalyticsUiState(
            spocs = sortedSpocs,
            totalSpocs = spocList.size,
            selectedEventId = selectedEventId,
            sortOption = sort
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpocAnalyticsUiState()
    )



    init {
        refresh()
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun setSelectedSpocEvent(eventId: Int?) {
        _spocFilter.update { it.copy(eventId = eventId) }
    }

    fun setSpocAnalyticsSort(sort: SpocAnalyticsSort) {
        _spocFilter.update { it.copy(sort = sort) }
    }

    fun setDirectorySearch(query: String) {
        _directorySearch.value = query
    }

    fun setFilterSpoc(spocId: String?) {
        _filterSpocId.value = spocId
    }

    fun setFilterTeam(teamId: String?) {
        _filterTeamId.value = teamId
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            var networkFailed = false

            // 1. Participant cache
            participantCacheRepository.refreshParticipants().onFailure { networkFailed = true }

            // 2. Attendance matrix
            attendanceMatrixRepository.refresh().onFailure { networkFailed = true }

            // 3. Admin directory
            directoryRepository.getAdminDirectoryData()
                .onSuccess { _directoryData.value = it }
                .onFailure { networkFailed = true }

            // 4. Events with stats
            attendanceRepository.syncAllEventsAttendance(forcePull = true).onFailure { networkFailed = true }

            _isOffline.value = networkFailed
            _isRefreshing.value = false
        }
    }

    fun createEvent(name: String, startsFrom: String, endsOn: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val event = Event(
                id = "",
                name = name,
                startsFrom = startsFrom,
                endsOn = endsOn,
                status = EventStatus.OPEN
            )
            manageEventsUseCase.createEvent(event)
                .onSuccess {
                    _actionMessage.emit("Event created successfully")
                    refresh()
                }
                .onFailure { error ->
                    _actionMessage.emit(error.message ?: "Failed to create event")
                }
            _isRefreshing.value = false
        }
    }

    fun updateEventTimeWindow(eventId: Int, startsFrom: String, endsOn: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            manageEventsUseCase.updateTimeWindow(eventId, startsFrom, endsOn)
                .onSuccess {
                    _actionMessage.emit("Time window updated")
                    refresh()
                }
                .onFailure { error ->
                    _actionMessage.emit(error.message ?: "Failed to update time window")
                }
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
