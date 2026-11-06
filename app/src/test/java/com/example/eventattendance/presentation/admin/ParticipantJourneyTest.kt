package com.example.eventattendance.presentation.admin

import com.example.eventattendance.domain.model.CachedParticipant
import com.example.eventattendance.domain.model.EventAttendance
import com.example.eventattendance.domain.model.TimeBasedStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ParticipantJourneyTest {

    @Test
    fun testParticipantJourneyChronologicalTimelineAndStats() {
        val participant = CachedParticipant(
            pId = "P101",
            name = "John Doe",
            phonenumber = "9876543210",
            teamId = "T01",
            teamName = "Alpha",
            teamLeader = "P101",
            spocId = "S01",
            spocName = "Jane Smith",
            spocPhonenumber = "9123456780"
        )

        val events = listOf(
            EventAttendance(
                eventId = 2,
                eventName = "Workshop",
                startsFrom = "2026-11-06T14:00:00",
                endsOn = "2026-11-06T16:00:00",
                presentPIds = setOf("P101")
            ),
            EventAttendance(
                eventId = 1,
                eventName = "Keynote",
                startsFrom = "2026-11-06T09:00:00",
                endsOn = "2026-11-06T11:00:00",
                presentPIds = setOf("P102")
            ),
            EventAttendance(
                eventId = 3,
                eventName = "Hackathon",
                startsFrom = "2026-11-06T17:00:00",
                endsOn = "2026-11-06T20:00:00",
                presentPIds = setOf("P101", "P103")
            )
        )

        // Sort events chronologically by startsFrom
        val sortedEvents = events.sortedWith(
            compareBy<EventAttendance> { it.startsFrom == null }
                .thenBy { it.startsFrom }
                .thenBy { it.eventId }
        )

        assertEquals(listOf(1, 2, 3), sortedEvents.map { it.eventId })

        val timeline = sortedEvents.map { event ->
            val isPresent = event.presentPIds.contains(participant.pId)
            ParticipantEventJourneyItem(
                eventId = event.eventId,
                eventName = event.eventName,
                startsFrom = event.startsFrom,
                endsOn = event.endsOn,
                formattedTimeWindow = "Time",
                timeStatus = TimeBasedStatus.ENDED,
                isPresent = isPresent
            )
        }

        val totalEvents = sortedEvents.size
        val attendedEvents = timeline.count { it.isPresent }
        val completionPercent = if (totalEvents > 0) attendedEvents.toFloat() / totalEvents.toFloat() else null
        val isLeader = participant.teamLeader == participant.pId

        val journey = ParticipantJourney(
            participant = participant,
            isLeader = isLeader,
            totalEvents = totalEvents,
            attendedEvents = attendedEvents,
            completionPercent = completionPercent,
            timeline = timeline
        )

        assertTrue(journey.isLeader)
        assertEquals(3, journey.totalEvents)
        assertEquals(2, journey.attendedEvents)
        assertEquals(2f / 3f, journey.completionPercent ?: 0f, 0.001f)

        // Event 1: Absent
        assertFalse(journey.timeline[0].isPresent)
        assertEquals(1, journey.timeline[0].eventId)

        // Event 2: Present
        assertTrue(journey.timeline[1].isPresent)
        assertEquals(2, journey.timeline[1].eventId)

        // Event 3: Present
        assertTrue(journey.timeline[2].isPresent)
        assertEquals(3, journey.timeline[2].eventId)
    }

    @Test
    fun testNeedsAttentionFiltering() {
        val criticalSpoc = SpocAnalytics(
            spocId = "S1",
            name = "SPOC 1",
            phone = null,
            teamCount = 1,
            studentCount = 10,
            checked = 2,
            total = 10,
            pct = 0.20f,
            pending = 8,
            status = SpocHealthStatus.CRITICAL
        )

        val onTrackSpoc = SpocAnalytics(
            spocId = "S2",
            name = "SPOC 2",
            phone = null,
            teamCount = 1,
            studentCount = 10,
            checked = 9,
            total = 10,
            pct = 0.90f,
            pending = 1,
            status = SpocHealthStatus.ON_TRACK
        )

        val spocs = listOf(criticalSpoc, onTrackSpoc)
        val needsAttention = spocs.filter {
            it.status == SpocHealthStatus.CRITICAL || it.status == SpocHealthStatus.NEEDS_ATTENTION
        }

        assertEquals(1, needsAttention.size)
        assertEquals("S1", needsAttention.first().spocId)
    }
}
