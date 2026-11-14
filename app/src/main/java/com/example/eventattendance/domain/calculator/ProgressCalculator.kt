package com.example.eventattendance.domain.calculator

import com.example.eventattendance.domain.model.CachedParticipant
import com.example.eventattendance.domain.model.EventAttendance
import com.example.eventattendance.domain.model.OverallResult
import com.example.eventattendance.domain.model.ProgressResult
import com.example.eventattendance.domain.model.TimeBasedStatus
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object ProgressCalculator {

    val IST_ZONE: ZoneId = ZoneId.of("Asia/Kolkata")

    fun nowIst(): LocalDateTime {
        return ZonedDateTime.now(IST_ZONE).toLocalDateTime()
    }

    /**
     * Parses event timestamp into an IST LocalDateTime.
     * Times are stored in DB as IST without timezone.
     * Replaces space with 'T', attempts LocalDateTime.parse, and falls back to OffsetDateTime.parse converted to IST.
     */
    fun parseToIstLocalDateTime(timestamp: String?): LocalDateTime? {
        if (timestamp.isNullOrBlank()) return null
        val normalized = timestamp.trim().replace(' ', 'T')
        return try {
            LocalDateTime.parse(normalized)
        } catch (_: Exception) {
            try {
                OffsetDateTime.parse(normalized).atZoneSameInstant(IST_ZONE).toLocalDateTime()
            } catch (_: Exception) {
                // If it has trailing fractional seconds or missing seconds
                try {
                    val clean = if (normalized.contains('.')) normalized.substringBefore('.') else normalized
                    LocalDateTime.parse(clean)
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    /**
     * Upcoming: before start (now < startsFrom)
     * Live: between start and end (startsFrom <= now && (endsOn == null || now <= endsOn))
     * Ended: after end (now > endsOn)
     */
    fun eventStatus(
        startsFrom: String?,
        endsOn: String?,
        now: LocalDateTime = nowIst()
    ): TimeBasedStatus {
        val start = parseToIstLocalDateTime(startsFrom)
        val end = parseToIstLocalDateTime(endsOn)

        if (start != null && now.isBefore(start)) {
            return TimeBasedStatus.UPCOMING
        }
        if (end != null && now.isAfter(end)) {
            return TimeBasedStatus.ENDED
        }
        return TimeBasedStatus.LIVE
    }

    fun isStarted(startsFrom: String?, now: LocalDateTime = nowIst()): Boolean {
        val start = parseToIstLocalDateTime(startsFrom) ?: return true
        return !now.isBefore(start)
    }

    /**
     * Sorts events chronologically based on their start time, then end time, then event ID.
     * Events with a start time are ordered chronologically; events without a start time are placed at the end.
     */
    fun <T> sortEventsByTime(
        events: List<T>,
        startsFrom: (T) -> String?,
        endsOn: (T) -> String? = { null },
        eventId: (T) -> Int = { 0 }
    ): List<T> {
        return events.sortedWith { a, b ->
            val aStart = parseToIstLocalDateTime(startsFrom(a))
            val bStart = parseToIstLocalDateTime(startsFrom(b))

            when {
                aStart != null && bStart != null -> {
                    val cmp = aStart.compareTo(bStart)
                    if (cmp != 0) {
                        cmp
                    } else {
                        val aEnd = parseToIstLocalDateTime(endsOn(a))
                        val bEnd = parseToIstLocalDateTime(endsOn(b))
                        if (aEnd != null && bEnd != null) {
                            val endCmp = aEnd.compareTo(bEnd)
                            if (endCmp != 0) endCmp else eventId(a).compareTo(eventId(b))
                        } else if (aEnd != null) -1
                        else if (bEnd != null) 1
                        else eventId(a).compareTo(eventId(b))
                    }
                }
                aStart != null -> -1
                bStart != null -> 1
                else -> eventId(a).compareTo(eventId(b))
            }
        }
    }

    /**
     * For a set of members M and an event E:
     * present = |M ∩ presentSet(E)|, total = |M|, percent = present / total
     */
    fun eventProgress(
        memberPIds: Set<String>,
        presentSet: Set<String>
    ): ProgressResult {
        val total = memberPIds.size
        val present = if (total > 0) memberPIds.count { it in presentSet } else 0
        val percent = if (total > 0) (present.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
        return ProgressResult(present = present, total = total, percent = percent)
    }

    /**
     * Overall progress for M = sum of present over started events / (|M| x number of started events).
     * If no event has started or M is empty, percent is null (show "-").
     */
    fun overallProgress(
        memberPIds: Set<String>,
        events: List<EventAttendance>,
        now: LocalDateTime = nowIst()
    ): OverallResult {
        val startedEvents = events.filter { isStarted(it.startsFrom, now) }
        val startedCount = startedEvents.size

        if (startedCount == 0 || memberPIds.isEmpty()) {
            return OverallResult(
                percent = null,
                presentSum = 0,
                expectedSum = 0,
                startedEventCount = startedCount
            )
        }

        val presentSum = startedEvents.sumOf { event ->
            memberPIds.count { it in event.presentPIds }
        }
        val expectedSum = memberPIds.size * startedCount
        val percent = if (expectedSum > 0) {
            (presentSum.toFloat() / expectedSum.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        return OverallResult(
            percent = percent,
            presentSum = presentSum,
            expectedSum = expectedSum,
            startedEventCount = startedCount
        )
    }

    /**
     * pendingMembers(M, E) = members of M not in presentSet(E).
     */
    fun pendingMembers(
        memberPIds: Set<String>,
        presentSet: Set<String>,
        allParticipants: Map<String, CachedParticipant>
    ): List<CachedParticipant> {
        val pendingIds = memberPIds - presentSet
        return pendingIds.map { pId ->
            allParticipants[pId] ?: CachedParticipant(pId = pId, name = pId)
        }.sortedBy { it.name ?: it.pId }
    }
}
