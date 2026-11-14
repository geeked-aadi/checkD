package com.example.eventattendance.presentation.eventdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.calculator.ProgressCalculator
import com.example.eventattendance.domain.model.AttendanceRecord
import com.example.eventattendance.domain.model.CachedParticipant
import com.example.eventattendance.domain.model.EventDetailData
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository
import com.example.eventattendance.domain.repository.AttendanceRepository
import com.example.eventattendance.domain.repository.AuthRepository
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val participantCacheRepository: ParticipantCacheRepository,
    private val authRepository: AuthRepository,
    private val attendanceMatrixRepository: AttendanceMatrixRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val eventId: String = checkNotNull(savedStateHandle["eventId"])
    val eventIdInt: Int = eventId.toIntOrNull() ?: 0
    val isScoped: Boolean = savedStateHandle["scoped"] ?: false

    val currentRole: StateFlow<UserRole> = authRepository.currentRole

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Checked in, 1: Pending
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val eventDetail: StateFlow<EventDetailData?> = combine(
        attendanceRepository.getEventAttendanceFlow(eventIdInt),
        attendanceMatrixRepository.matrixFlow,
        participantCacheRepository.participantsFlow
    ) { repoDetail, matrix, cache ->
        val matrixEvent = matrix?.events?.firstOrNull { it.eventId == eventIdInt }
        val totalExpected = if (matrix?.totalParticipants != null && matrix.totalParticipants > 0) {
            matrix.totalParticipants
        } else if (cache.isNotEmpty()) {
            cache.size
        } else {
            repoDetail?.totalExpected ?: 0
        }

        if (repoDetail != null && repoDetail.attendees.isNotEmpty()) {
            val existingPids = repoDetail.attendees.map { it.participantId }.toSet()
            val missingFromRepo = (matrixEvent?.presentPIds ?: emptySet()) - existingPids

            val additionalAttendees = missingFromRepo.map { pId ->
                val p = cache[pId]
                AttendanceRecord(
                    id = "${eventIdInt}_$pId",
                    eventId = eventId,
                    participantId = pId,
                    checkedInAt = "",
                    status = "present",
                    participantName = p?.name ?: pId,
                    participantUsn = pId,
                    teamId = p?.teamId,
                    teamName = p?.teamName,
                    isPendingSync = false
                )
            }

            val enrichedAttendees = repoDetail.attendees.map { record ->
                if (record.participantName == record.participantId || record.teamName == null) {
                    val p = cache[record.participantId]
                    if (p != null) {
                        record.copy(
                            participantName = if (record.participantName == record.participantId) (p.name ?: p.pId) else record.participantName,
                            teamId = record.teamId ?: p.teamId,
                            teamName = record.teamName ?: p.teamName
                        )
                    } else {
                        record
                    }
                } else {
                    record
                }
            }

            val allAttendees = additionalAttendees + enrichedAttendees
            val actualScanned = allAttendees.size

            repoDetail.copy(
                eventName = if (repoDetail.eventName.startsWith("Event ") && matrixEvent?.eventName != null) {
                    matrixEvent.eventName
                } else repoDetail.eventName,
                startsFrom = repoDetail.startsFrom ?: matrixEvent?.startsFrom,
                endsOn = repoDetail.endsOn ?: matrixEvent?.endsOn,
                scannedCount = actualScanned,
                totalExpected = if (totalExpected > 0) totalExpected else repoDetail.totalExpected,
                progress = "$actualScanned/$totalExpected",
                attendees = allAttendees
            )
        } else if (matrixEvent != null) {
            val presentIds = matrixEvent.presentPIds
            val attendees = presentIds.map { pId ->
                val p = cache[pId]
                AttendanceRecord(
                    id = "${eventIdInt}_$pId",
                    eventId = eventId,
                    participantId = pId,
                    checkedInAt = "",
                    status = "present",
                    participantName = p?.name ?: pId,
                    participantUsn = pId,
                    teamId = p?.teamId,
                    teamName = p?.teamName,
                    isPendingSync = false
                )
            }.reversed()

            EventDetailData(
                eventId = eventIdInt,
                eventName = matrixEvent.eventName,
                startsFrom = matrixEvent.startsFrom,
                endsOn = matrixEvent.endsOn,
                scannedCount = presentIds.size,
                totalExpected = totalExpected,
                progress = "${presentIds.size}/$totalExpected",
                attendees = attendees
            )
        } else {
            repoDetail
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Current SPOC member IDs (empty if not scoped or not SPOC)
    val scopedMemberPIds: StateFlow<Set<String>> = combine(
        authRepository.currentRole,
        participantCacheRepository.participantsFlow
    ) { role, _ ->
        if (isScoped && role is UserRole.Spoc) {
            participantCacheRepository.getMemberPIds(role.spocId)
        } else {
            emptySet()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    // Filtered Checked-In Attendees
    val filteredAttendanceList: StateFlow<List<AttendanceRecord>> = combine(
        eventDetail,
        scopedMemberPIds,
        _searchQuery
    ) { detail, spocMembers, query ->
        val attendees = detail?.attendees ?: emptyList()
        val scopedList = if (isScoped && spocMembers.isNotEmpty()) {
            attendees.filter { it.participantId in spocMembers }
        } else {
            attendees
        }

        if (query.isBlank()) {
            scopedList
        } else {
            val q = query.trim().lowercase()
            scopedList.filter { record ->
                (record.participantName?.lowercase()?.contains(q) == true) ||
                (record.participantUsn?.lowercase()?.contains(q) == true) ||
                (record.teamName?.lowercase()?.contains(q) == true) ||
                (record.teamId?.lowercase()?.contains(q) == true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Pending Attendees
    val filteredPendingList: StateFlow<List<CachedParticipant>> = combine(
        eventDetail,
        scopedMemberPIds,
        participantCacheRepository.participantsFlow,
        _searchQuery
    ) { detail, spocMembers, cache, query ->
        val presentIds = detail?.attendees?.map { it.participantId }?.toSet() ?: emptySet()

        val pendingList = if (isScoped && spocMembers.isNotEmpty()) {
            ProgressCalculator.pendingMembers(spocMembers, presentIds, cache)
        } else {
            val allIds = cache.keys
            ProgressCalculator.pendingMembers(allIds, presentIds, cache)
        }

        if (query.isBlank()) {
            pendingList
        } else {
            val q = query.trim().lowercase()
            pendingList.filter { p ->
                (p.name?.lowercase()?.contains(q) == true) ||
                (p.pId.lowercase().contains(q)) ||
                (p.teamName?.lowercase()?.contains(q) == true) ||
                (p.teamId?.lowercase()?.contains(q) == true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Scoped Stats
    val scopedScannedCount: StateFlow<Int> = combine(
        eventDetail,
        scopedMemberPIds
    ) { detail, spocMembers ->
        if (isScoped && spocMembers.isNotEmpty()) {
            detail?.attendees?.count { it.participantId in spocMembers } ?: 0
        } else {
            detail?.scannedCount ?: 0
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val scopedTotalCount: StateFlow<Int> = combine(
        eventDetail,
        scopedMemberPIds,
        participantCacheRepository.participantsFlow
    ) { detail, spocMembers, cache ->
        if (isScoped && spocMembers.isNotEmpty()) {
            spocMembers.size
        } else {
            detail?.totalExpected ?: cache.size
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
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
            attendanceRepository.syncEventAttendance(eventIdInt)
            attendanceMatrixRepository.refresh()
            _isRefreshing.value = false
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun getPhoneNumber(pId: String): String? {
        return participantCacheRepository.getParticipant(pId)?.phonenumber
    }
}
