package com.example.eventattendance.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.domain.model.EventStatus
import com.example.eventattendance.domain.model.EventWithStats
import com.example.eventattendance.domain.usecase.AdminLoginUseCase
import com.example.eventattendance.domain.usecase.GetEventsWithStatsUseCase
import com.example.eventattendance.domain.usecase.ManageEventsUseCase
import com.example.eventattendance.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    getEventsWithStatsUseCase: GetEventsWithStatsUseCase,
    private val manageEventsUseCase: ManageEventsUseCase,
    private val adminLoginUseCase: AdminLoginUseCase,
    private val syncManager: SyncManager
) : ViewModel() {

    val eventsWithStats: StateFlow<List<EventWithStats>> = getEventsWithStatsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isCreatingEvent = MutableStateFlow(false)
    val isCreatingEvent: StateFlow<Boolean> = _isCreatingEvent.asStateFlow()

    fun showCreateDialog() {
        _isCreatingEvent.value = true
    }

    fun hideCreateDialog() {
        _isCreatingEvent.value = false
    }

    fun createEvent(event: Event) {
        viewModelScope.launch {
            _isCreatingEvent.value = false
            manageEventsUseCase.createEvent(event)
            syncManager.syncAll(forcePull = true)
        }
    }

    fun toggleEventStatus(eventId: String, currentStatus: EventStatus) {
        viewModelScope.launch {
            val newIsOpen = currentStatus != EventStatus.OPEN
            manageEventsUseCase.toggleEventStatus(eventId, newIsOpen)
            syncManager.syncAll(forcePull = true)
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            adminLoginUseCase.logout()
            onSuccess()
        }
    }
}
