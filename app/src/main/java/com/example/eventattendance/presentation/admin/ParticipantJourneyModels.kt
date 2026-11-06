package com.example.eventattendance.presentation.admin

import com.example.eventattendance.domain.model.CachedParticipant
import com.example.eventattendance.domain.model.TimeBasedStatus

data class ParticipantEventJourneyItem(
    val eventId: Int,
    val eventName: String,
    val startsFrom: String?,
    val endsOn: String?,
    val formattedTimeWindow: String,
    val timeStatus: TimeBasedStatus, // UPCOMING, LIVE, ENDED
    val isPresent: Boolean
)

data class ParticipantJourney(
    val participant: CachedParticipant,
    val isLeader: Boolean,
    val totalEvents: Int,
    val attendedEvents: Int,
    val completionPercent: Float?, // null if 0 events; 0.0f..1.0f
    val timeline: List<ParticipantEventJourneyItem>
)
