package com.ndhunju.dailyjournal.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.AssertionError
import java.lang.Exception
import java.util.*


internal class EnglishToNepaliDateConverterTest {

    @Test
    fun convertToNepali_lowerDateOutOfBound() {

        try {
            val (_, _, _) = EnglishToNepaliDateConverter.convertToNepaliUsingHumanReadableValues(
                englishYear = 2017,
                englishMonth = 1,
                englishDay = 1
            )
        } catch (ex: Exception) {
            assertEquals(
                "Incorrect exception",
                IndexOutOfBoundsException().javaClass,
                ex.javaClass
            )

            return
        }

        throw AssertionError("IndexOutOfBoundsException expected")
    }

    @Test
    fun convertToNepali_higherDateOutOfBound() {

        try {
            val (_, _, _) = EnglishToNepaliDateConverter.convertToNepaliUsingHumanReadableValues(
                englishYear = 2026,
                englishMonth = 1,
                englishDay = 1
            )
        } catch (ex: Exception) {
            assertEquals(
                "Incorrect exception",
                IndexOutOfBoundsException().javaClass,
                ex.javaClass
            )

            return
        }

        throw AssertionError("IndexOutOfBoundsException expected")
    }

    @Test
    fun convertToNepali0() {

        assertEquals(
            englishYear = 2018,
            englishMonth = 4,
            englishDay = 15,
            expectedNepaliYear = 2075,
            expectedNepaliMonth = 1,
            expectedNepaliDay = 2
        )
    }

    @Test
    fun convertToNepali1() {

        assertEquals(
            englishYear = 2022,
            englishMonth = 11,
            englishDay = 28,
            expectedNepaliYear = 2079,
            expectedNepaliMonth = 8,
            expectedNepaliDay = 12
        )
    }

    @Test
    fun convertToNepali2() {

        assertEquals(
            englishYear = 2022,
            englishMonth = 12,
            englishDay = 1,
            expectedNepaliYear = 2079,
            expectedNepaliMonth = 8,
            expectedNepaliDay = 15
        )
    }

    @Test
    fun convertToNepali3() {

        assertEquals(
            englishYear = 2022,
            englishMonth = 11,
            englishDay = 28,
            expectedNepaliYear = 2079,
            expectedNepaliMonth = 8,
            expectedNepaliDay = 12
        )

    }

    @Test
    fun convertToNepali4() {

        assertEquals(
            englishYear = 2018,
            englishMonth = 4,
            englishDay = 20,
            expectedNepaliYear = 2075,
            expectedNepaliMonth = 1,
            expectedNepaliDay = 7
        )

    }

    @Test
    fun convertToNepali5() {

        assertEquals(
            englishYear = 2025,
            englishMonth = 2,
            englishDay = 12,
            expectedNepaliYear = 2081,
            expectedNepaliMonth = 10,
            expectedNepaliDay = 30
        )


    }

    @Test
    fun convertToNepali6() {

        assertEquals(
            englishYear = 2022,
            englishMonth = 11,
            englishDay = 28,
            expectedNepaliYear = 2079,
            expectedNepaliMonth = 8,
            expectedNepaliDay = 12
        )


    }

    private fun assertEquals(
        englishYear: Int,
        englishMonth: Int,
        englishDay: Int,
        expectedNepaliYear: Int,
        expectedNepaliMonth: Int,
        expectedNepaliDay: Int
    ) {
        val (nepaliYear, nepaliMonth, nepaliDay) = EnglishToNepaliDateConverter
            .convertToNepaliUsingHumanReadableValues(
                englishYear = englishYear,
                englishMonth = englishMonth,
                englishDay = englishDay
            )

        assertEquals("year doesn't match", expectedNepaliYear , nepaliYear)
        assertEquals("month doesn't match", expectedNepaliMonth , nepaliMonth)
        assertEquals("day doesn't match", expectedNepaliDay , nepaliDay)
    }

    @Test
    fun foo() {
        val startDateCalendar = GregorianCalendar()
        startDateCalendar.set(2023, 1, 1)

        val endDateCalendar = GregorianCalendar()
        endDateCalendar.set(2023, 1, 1)

        val numOfDays = EnglishToNepaliDateConverter.numOfDaysBetween(
            startDateCalendar,
            endDateCalendar
        )

        assertEquals(0, numOfDays)
    }

    @Test
    fun foo1() {
        val startDateCalendar = GregorianCalendar()
        startDateCalendar.set(2023, 1, 1)

        val endDateCalendar = GregorianCalendar()
        endDateCalendar.set(2023, 1, 2)

        val numOfDays = EnglishToNepaliDateConverter.numOfDaysBetween(
            startDateCalendar,
            endDateCalendar
        )

        assertEquals(1, numOfDays)
    }

    @Test
    fun foo2() {
        val startDateCalendar = GregorianCalendar()
        startDateCalendar.set(2023, 1, 1)

        val endDateCalendar = GregorianCalendar()
        endDateCalendar.set(2023, 2, 2)

        val numOfDays = EnglishToNepaliDateConverter.numOfDaysBetween(
            startDateCalendar,
            endDateCalendar
        )

        assertEquals(29, numOfDays)
    }

    @Test
    fun foo3() {
        val startDateCalendar = GregorianCalendar()
        startDateCalendar.set(2023, 0, 1)

        val endDateCalendar = GregorianCalendar()
        endDateCalendar.set(2023, 2, 3)

        val numOfDays = EnglishToNepaliDateConverter.numOfDaysBetween(
            startDateCalendar,
            endDateCalendar
        )

        assertEquals(61, numOfDays)
    }
}