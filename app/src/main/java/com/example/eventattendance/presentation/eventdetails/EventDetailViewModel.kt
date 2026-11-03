package com.example.eventattendance.presentation.eventdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventattendance.domain.model.AttendanceRecord
import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.domain.usecase.GetEventDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val getEventDetailUseCase: GetEventDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val eventId: String = checkNotNull(savedStateHandle["eventId"])

    val event: StateFlow<Event?> = getEventDetailUseCase.getEvent(eventId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val scannedCount: StateFlow<Int> = getEventDetailUseCase.getScannedCount(eventId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val rawAttendanceList = getEventDetailUseCase.getAttendanceList(eventId)

    val filteredAttendanceList: StateFlow<List<AttendanceRecord>> = combine(
        rawAttendanceList,
        _searchQuery
    ) { records, query ->
        if (query.isBlank()) {
            records
        } else {
            val q = query.trim().lowercase()
            records.filter { record ->
                (record.participantName?.lowercase()?.contains(q) == true) ||
                (record.participantUsn?.lowercase()?.contains(q) == true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
