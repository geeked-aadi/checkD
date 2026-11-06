package com.example.eventattendance.presentation.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.calculator.ProgressCalculator
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository
import com.example.eventattendance.domain.repository.AuthRepository
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import com.example.eventattendance.util.DateUtils
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
class ParticipantDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val participantCacheRepository: ParticipantCacheRepository,
    private val attendanceMatrixRepository: AttendanceMatrixRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val pId: String = checkNotNull(savedStateHandle["pId"])

    val currentRole: StateFlow<UserRole> = authRepository.currentRole

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val participantJourney: StateFlow<ParticipantJourney?> = combine(
        participantCacheRepository.participantsFlow,
        attendanceMatrixRepository.matrixFlow
    ) { cache, matrixData ->
        val participant = cache[pId] ?: cache.values.find { it.pId.equals(pId, ignoreCase = true) }
            ?: return@combine null

        val events = matrixData?.events ?: emptyList()
        // Sort chronologically by startsFrom
        val sortedEvents = events.sortedWith(
            compareBy<com.example.eventattendance.domain.model.EventAttendance> { it.startsFrom == null }
                .thenBy { it.startsFrom }
                .thenBy { it.eventId }
        )

        val timeline = sortedEvents.map { event ->
            val isPresent = event.presentPIds.contains(participant.pId)
            val timeStatus = ProgressCalculator.eventStatus(event.startsFrom, event.endsOn)
            val formattedTime = DateUtils.formatTimeWindow(event.startsFrom, event.endsOn)

            ParticipantEventJourneyItem(
                eventId = event.eventId,
                eventName = event.eventName,
                startsFrom = event.startsFrom,
                endsOn = event.endsOn,
                formattedTimeWindow = formattedTime,
                timeStatus = timeStatus,
                isPresent = isPresent
            )
        }

        val totalEvents = sortedEvents.size
        val attendedEvents = timeline.count { it.isPresent }
        val completionPercent = if (totalEvents > 0) {
            (attendedEvents.toFloat() / totalEvents.toFloat()).coerceIn(0f, 1f)
        } else {
            null
        }

        val isLeader = participant.teamLeader == participant.pId ||
                participant.teamLeader?.trim()?.equals(participant.name?.trim(), ignoreCase = true) == true

        ParticipantJourney(
            participant = participant,
            isLeader = isLeader,
            totalEvents = totalEvents,
            attendedEvents = attendedEvents,
            completionPercent = completionPercent,
            timeline = timeline
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            runCatching {
                attendanceMatrixRepository.refresh()
                participantCacheRepository.refreshParticipants()
            }
            _isRefreshing.value = false
        }
    }
}
