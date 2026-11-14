package com.example.eventattendance.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DateUtilsTest {

    @Test
    fun testFormatTimeWindowIncludesDate() {
        val formatted = DateUtils.formatTimeWindow(
            startsFrom = "2026-11-06T09:30:00",
            endsOn = "2026-11-06T18:45:00"
        )

        assertEquals("06 Nov 2026, 9:30 AM – 6:45 PM", formatted)
    }

    @Test
    fun testFormatTimeWindowTwoDayRange() {
        val formatted = DateUtils.formatTimeWindow(
            startsFrom = "2026-11-06T09:30:00",
            endsOn = "2026-11-07T12:15:00"
        )

        assertEquals("06 Nov 2026, 9:30 AM – 07 Nov 2026, 12:15 PM", formatted)
    }

    @Test
    fun testFormatTimeWindowWithSpaceSeparator() {
        val formatted = DateUtils.formatTimeWindow(
            startsFrom = "2026-11-06 09:30:00",
            endsOn = "2026-11-06 18:45:00"
        )

        assertEquals("06 Nov 2026, 9:30 AM – 6:45 PM", formatted)
    }

    @Test
    fun testFormatTimeWindowStartOnly() {
        val formatted = DateUtils.formatTimeWindow(
            startsFrom = "2026-11-06T09:30:00",
            endsOn = null
        )

        assertEquals("Starts 06 Nov 2026, 9:30 AM", formatted)
    }

    @Test
    fun testFormatTimeWindowEndOnly() {
        val formatted = DateUtils.formatTimeWindow(
            startsFrom = null,
            endsOn = "2026-11-06T18:45:00"
        )

        assertEquals("Until 06 Nov 2026, 6:45 PM", formatted)
    }

    @Test
    fun testFormatTimeWindowNullAndBlank() {
        assertEquals("", DateUtils.formatTimeWindow(null, null))
        assertEquals("", DateUtils.formatTimeWindow("", "   "))
    }

    @Test
    fun testFormatToTimeOnly() {
        assertEquals("9:30 AM", DateUtils.formatToTimeOnly("2026-11-06T09:30:00"))
        assertEquals("Just now", DateUtils.formatToTimeOnly(null))
        assertEquals("Just now", DateUtils.formatToTimeOnly(""))
    }

    @Test
    fun testGetCurrentIsoTime() {
        val iso = DateUtils.getCurrentIsoTime()
        org.junit.Assert.assertNotNull(iso)
        org.junit.Assert.assertTrue(iso.contains("T"))
        org.junit.Assert.assertTrue(iso.endsWith("Z"))
    }
}
