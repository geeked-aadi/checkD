package com.example.eventattendance.domain.calculator

import com.example.eventattendance.domain.model.CachedParticipant
import com.example.eventattendance.domain.model.EventAttendance
import com.example.eventattendance.domain.model.TimeBasedStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class ProgressCalculatorTest {

    @Test
    fun testEmptyTeam() {
        val members = emptySet<String>()
        val present = setOf("p1", "p2")

        val eventResult = ProgressCalculator.eventProgress(members, present)
        assertEquals(0, eventResult.present)
        assertEquals(0, eventResult.total)
        assertEquals(0f, eventResult.percent, 0.001f)
        assertEquals("0/0", eventResult.displayFraction)

        val events = listOf(
            EventAttendance(
                eventId = 1,
                eventName = "Event 1",
                startsFrom = "2026-11-06T09:00:00",
                endsOn = "2026-11-06T18:00:00",
                presentPIds = present
            )
        )
        val now = LocalDateTime.parse("2026-11-06T12:00:00")
        val overall = ProgressCalculator.overallProgress(members, events, now)
        assertNull(overall.percent)
        assertEquals("-", overall.displayPercent)
    }

    @Test
    fun testNoStartedEvents() {
        val members = setOf("p1", "p2", "p3")
        val events = listOf(
            EventAttendance(
                eventId = 1,
                eventName = "Event 1",
                startsFrom = "2026-11-07T09:00:00",
                endsOn = "2026-11-07T18:00:00",
                presentPIds = emptySet()
            ),
            EventAttendance(
                eventId = 2,
                eventName = "Event 2",
                startsFrom = "2026-11-08T09:00:00",
                endsOn = "2026-11-08T18:00:00",
                presentPIds = emptySet()
            )
        )
        val now = LocalDateTime.parse("2026-11-06T12:00:00")
        val overall = ProgressCalculator.overallProgress(members, events, now)

        assertNull(overall.percent)
        assertEquals("-", overall.displayPercent)
        assertEquals(0, overall.startedEventCount)
    }

    @Test
    fun testPartialAttendance() {
        val members = setOf("p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10")
        val present = setOf("p1", "p2", "p3", "other1", "other2")

        val result = ProgressCalculator.eventProgress(members, present)
        assertEquals(3, result.present)
        assertEquals(10, result.total)
        assertEquals(0.3f, result.percent, 0.001f)
        assertEquals(30, result.percentageInt)
        assertEquals("3/10", result.displayFraction)
    }

    @Test
    fun testSpocAggregate() {
        val spocMembers = setOf("p1", "p2", "p3", "p4") // 4 members across all teams
        val events = listOf(
            EventAttendance(
                eventId = 1,
                eventName = "Keynote",
                startsFrom = "2026-11-06T09:00:00",
                endsOn = "2026-11-06T11:00:00",
                presentPIds = setOf("p1", "p2", "p3") // 3 present
            ),
            EventAttendance(
                eventId = 2,
                eventName = "Workshop",
                startsFrom = "2026-11-06T11:30:00",
                endsOn = "2026-11-06T13:30:00",
                presentPIds = setOf("p1", "p2", "p4") // 3 present
            ),
            EventAttendance(
                eventId = 3,
                eventName = "Future Event",
                startsFrom = "2026-11-07T09:00:00",
                endsOn = "2026-11-07T12:00:00",
                presentPIds = emptySet() // Not started
            )
        )
        val now = LocalDateTime.parse("2026-11-06T12:00:00")
        val overall = ProgressCalculator.overallProgress(spocMembers, events, now)

        // 2 started events, total expected = 4 * 2 = 8, present sum = 3 + 3 = 6
        assertEquals(2, overall.startedEventCount)
        assertEquals(6, overall.presentSum)
        assertEquals(8, overall.expectedSum)
        assertEquals(0.75f, overall.percent!!, 0.001f)
        assertEquals("75%", overall.displayPercent)
    }

    @Test
    fun testPendingMembers() {
        val members = setOf("p1", "p2", "p3")
        val present = setOf("p2")
        val cache = mapOf(
            "p1" to CachedParticipant(pId = "p1", name = "Alice", phonenumber = "1234567890"),
            "p2" to CachedParticipant(pId = "p2", name = "Bob", phonenumber = "0987654321"),
            "p3" to CachedParticipant(pId = "p3", name = "Charlie", phonenumber = "1122334455")
        )

        val pending = ProgressCalculator.pendingMembers(members, present, cache)
        assertEquals(2, pending.size)
        assertEquals(listOf("p1", "p3"), pending.map { it.pId })
        assertEquals(listOf("Alice", "Charlie"), pending.map { it.name })
    }

    @Test
    fun testTimeBasedStatus() {
        val now = LocalDateTime.parse("2026-11-06T12:00:00")

        // Upcoming
        val statusUpcoming = ProgressCalculator.eventStatus(
            startsFrom = "2026-11-06T14:00:00",
            endsOn = "2026-11-06T16:00:00",
            now = now
        )
        assertEquals(TimeBasedStatus.UPCOMING, statusUpcoming)

        // Live
        val statusLive = ProgressCalculator.eventStatus(
            startsFrom = "2026-11-06T11:00:00",
            endsOn = "2026-11-06T13:00:00",
            now = now
        )
        assertEquals(TimeBasedStatus.LIVE, statusLive)

        // Ended
        val statusEnded = ProgressCalculator.eventStatus(
            startsFrom = "2026-11-06T09:00:00",
            endsOn = "2026-11-06T11:00:00",
            now = now
        )
        assertEquals(TimeBasedStatus.ENDED, statusEnded)
    }

    @Test
    fun testIsoDateParsingWithSpaceAndOffsetFallback() {
        val parsedSpace = ProgressCalculator.parseToIstLocalDateTime("2026-11-06 09:30:00")
        assertEquals(LocalDateTime.parse("2026-11-06T09:30:00"), parsedSpace)

        val parsedIso = ProgressCalculator.parseToIstLocalDateTime("2026-11-06T09:30:00")
        assertEquals(LocalDateTime.parse("2026-11-06T09:30:00"), parsedIso)

        // UTC offset converted to IST (+05:30)
        val parsedUtc = ProgressCalculator.parseToIstLocalDateTime("2026-11-06T04:00:00Z")
        assertEquals(LocalDateTime.parse("2026-11-06T09:30:00"), parsedUtc)
    }

    @Test
    fun testSortEventsByTime() {
        data class TestEvent(
            val id: Int,
            val start: String?,
            val end: String?
        )

        val events = listOf(
            TestEvent(id = 10, start = "2026-11-07 10:00:00", end = "2026-11-07 12:00:00"),
            TestEvent(id = 2, start = "2026-11-06 14:00:00", end = "2026-11-06 16:00:00"),
            TestEvent(id = 99, start = "2026-11-05 09:00:00", end = "2026-11-05 11:00:00"),
            TestEvent(id = 5, start = "2026-11-06 09:00:00", end = "2026-11-06 12:00:00"),
            TestEvent(id = 7, start = "2026-11-06 09:00:00", end = "2026-11-06 10:00:00"),
            TestEvent(id = 1, start = null, end = null),
            TestEvent(id = 4, start = "invalid-date", end = null)
        )

        val sorted = ProgressCalculator.sortEventsByTime(
            events = events,
            startsFrom = { it.start },
            endsOn = { it.end },
            eventId = { it.id }
        )

        // Expected order:
        // 1. Nov 5 09:00 (id 99)
        // 2. Nov 6 09:00 ends 10:00 (id 7)
        // 3. Nov 6 09:00 ends 12:00 (id 5)
        // 4. Nov 6 14:00 (id 2)
        // 5. Nov 7 10:00 (id 10)
        // 6 & 7: invalid and null timestamps sorted at end with id tie-breaker (id 1, then id 4)
        assertEquals(listOf(99, 7, 5, 2, 10, 1, 4), sorted.map { it.id })
    }
}
