package com.example.eventattendance.presentation.admin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpocAnalyticsTest {

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
    }

    @Test
    fun testPercentageAndPendingCalculation() {
        val studentCount = 10
        val eventCount = 2
        val total = studentCount * eventCount
        val checked = 15
        val pending = if (total > 0) (total - checked).coerceAtLeast(0) else 0
        val pct = if (total > 0) (checked.toFloat() / total.toFloat()).coerceIn(0f, 1f) else null

        assertEquals(20, total)
        assertEquals(5, pending)
        assertEquals(0.75f, pct ?: 0f, 0.001f)
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
            pending = 2
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
            pending = 7
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
            pending = 0
        )

        val spocs = listOf(spoc1, spoc2, spoc3)

        // Lowest completion first (pct == null at bottom)
        val sortedLowest = spocs.sortedWith(
            compareBy<SpocAnalytics> { it.pct == null }
                .thenBy { it.pct ?: 0f }
                .thenByDescending { it.pending }
                .thenBy { it.name.lowercase() }
        )
        assertEquals(listOf("spoc2", "spoc1", "spoc3"), sortedLowest.map { it.spocId })

        // Highest completion first (pct == null at bottom)
        val sortedHighest = spocs.sortedWith(
            compareBy<SpocAnalytics> { it.pct == null }
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
