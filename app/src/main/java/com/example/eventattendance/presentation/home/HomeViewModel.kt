package com.example.eventattendance.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.model.EventWithStats
import com.example.eventattendance.domain.usecase.GetEventsWithStatsUseCase
import com.example.eventattendance.domain.usecase.SyncDataUseCase
import com.example.eventattendance.sync.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getEventsWithStatsUseCase: GetEventsWithStatsUseCase,
    private val syncDataUseCase: SyncDataUseCase
) : ViewModel() {

    val eventsWithStats: StateFlow<List<EventWithStats>> = getEventsWithStatsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isOffline: StateFlow<Boolean> = syncDataUseCase.observeConnectivity()
        .map { it != ConnectivityObserver.Status.Available }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val pendingScansCount: StateFlow<Int> = syncDataUseCase.getPendingCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        // Trigger initial background sync on app launch
        refreshEvents(force = false)
    }

    fun refreshEvents(force: Boolean = true) {
        viewModelScope.launch {
            _isRefreshing.value = true
            syncDataUseCase.triggerSync(forcePull = force)
            _isRefreshing.value = false
        }
    }
}
