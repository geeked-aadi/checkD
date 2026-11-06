package com.example.eventattendance.presentation.admin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpocAnalyticsTest {

    private fun computeStatus(teamCount: Int, studentCount: Int, pct: Float?): SpocHealthStatus {
        return when {
            teamCount == 0 -> SpocHealthStatus.NO_TEAMS
            studentCount == 0 -> SpocHealthStatus.NO_TEAMS
            pct == null -> SpocHealthStatus.NO_TEAMS
            pct >= SpocAnalyticsThresholds.ON_TRACK -> SpocHealthStatus.ON_TRACK
            pct >= SpocAnalyticsThresholds.NEEDS_ATTENTION -> SpocHealthStatus.NEEDS_ATTENTION
            else -> SpocHealthStatus.CRITICAL
        }
    }

    @Test
    fun testStatusThresholds() {
        assertEquals(0.70f, SpocAnalyticsThresholds.ON_TRACK, 0.001f)
        assertEquals(0.40f, SpocAnalyticsThresholds.NEEDS_ATTENTION, 0.001f)

        // >= 70% is On Track
        assertEquals(SpocHealthStatus.ON_TRACK, computeStatus(2, 10, 0.85f))
        assertEquals(SpocHealthStatus.ON_TRACK, computeStatus(2, 10, 0.70f))

        // 40% - 69% is Needs Attention
        assertEquals(SpocHealthStatus.NEEDS_ATTENTION, computeStatus(2, 10, 0.69f))
        assertEquals(SpocHealthStatus.NEEDS_ATTENTION, computeStatus(2, 10, 0.40f))

        // < 40% is Critical
        assertEquals(SpocHealthStatus.CRITICAL, computeStatus(2, 10, 0.39f))
        assertEquals(SpocHealthStatus.CRITICAL, computeStatus(2, 10, 0.00f))

        // No teams or 0 students is NO_TEAMS
        assertEquals(SpocHealthStatus.NO_TEAMS, computeStatus(0, 0, null))
        assertEquals(SpocHealthStatus.NO_TEAMS, computeStatus(1, 0, null))
    }

    @Test
    fun testDivisionByZeroHandled() {
        val studentCount = 0
        val eventCount = 3
        val total = studentCount * eventCount
        val checked = 0
        val pending = if (total > 0) (total - checked).coerceAtLeast(0) else 0
        val pct = if (total > 0) (checked.toFloat() / total.toFloat()).coerceIn(0f, 1f) else null

        assertEquals(0, total)
        assertEquals(0, pending)
        assertNull(pct)
        assertEquals(SpocHealthStatus.NO_TEAMS, computeStatus(0, studentCount, pct))
    }

    @Test
    fun testSorting() {
        val spoc1 = SpocAnalytics(
            spocId = "spoc1",
            name = "Charlie",
            phone = null,
            teamCount = 2,
            studentCount = 10,
            checked = 8,
            total = 10,
            pct = 0.80f,
            pending = 2,
            status = SpocHealthStatus.ON_TRACK
        )

        val spoc2 = SpocAnalytics(
            spocId = "spoc2",
            name = "Alice",
            phone = null,
            teamCount = 2,
            studentCount = 10,
            checked = 3,
            total = 10,
            pct = 0.30f,
            pending = 7,
            status = SpocHealthStatus.CRITICAL
        )

        val spoc3 = SpocAnalytics(
            spocId = "spoc3",
            name = "Bob",
            phone = null,
            teamCount = 0,
            studentCount = 0,
            checked = 0,
            total = 0,
            pct = null,
            pending = 0,
            status = SpocHealthStatus.NO_TEAMS
        )

        val spocs = listOf(spoc1, spoc2, spoc3)

        // Lowest completion first (needs attention / critical on top, NO_TEAMS at bottom)
        val sortedLowest = spocs.sortedWith(
            compareBy<SpocAnalytics> { it.status == SpocHealthStatus.NO_TEAMS }
                .thenBy { it.pct ?: 0f }
                .thenByDescending { it.pending }
                .thenBy { it.name.lowercase() }
        )
        assertEquals(listOf("spoc2", "spoc1", "spoc3"), sortedLowest.map { it.spocId })

        // Highest completion first
        val sortedHighest = spocs.sortedWith(
            compareBy<SpocAnalytics> { it.status == SpocHealthStatus.NO_TEAMS }
                .thenByDescending { it.pct ?: 0f }
                .thenBy { it.pending }
                .thenBy { it.name.lowercase() }
        )
        assertEquals(listOf("spoc1", "spoc2", "spoc3"), sortedHighest.map { it.spocId })

        // Name sorting
        val sortedName = spocs.sortedBy { it.name.lowercase() }
        assertEquals(listOf("spoc2", "spoc3", "spoc1"), sortedName.map { it.spocId })
    }
}
