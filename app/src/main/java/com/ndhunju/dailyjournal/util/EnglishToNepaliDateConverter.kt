package com.ndhunju.dailyjournal.util

import android.util.Log
import com.ndhunju.dailyjournal.BuildConfig
import com.ndhunju.dailyjournal.extensions.beforeUpToDays
import com.ndhunju.dailyjournal.extensions.getHumanReadableValue
import com.ndhunju.dailyjournal.extensions.setHumanReadableValues
import java.util.*

/**
 * Provides functionality to convert English date to Nepali date
 */
object EnglishToNepaliDateConverter {

    private var nepaliYearToNumOfDaysInMonthMap: MutableMap<Int, IntArray> = HashMap()

    // Taking Baisakh 1, 2075 as base Nepali date
    private var baseNepaliYear = 2075
    private var baseNepaliMonth = 1
    private var baseNepaliDay = 1

    // Base English date would be April 14th, 2018
    private var baseEnglishYear = 2018
    private var baseEnglishMonth = 4
    private var baseEnglishDay = 14
    private var baseEnglishDayOfWeek = Calendar.SATURDAY // April 14, 2018 is Saturday

    // Taking Chaitra 2081 as the maximum Nepali date
    private var maxNepaliYear = 2081
    private var maxNepaliMonth = 12
    private var maxNepaliDay = 30

    // Max English date would be April 13th, 2025
    private var maxEnglishYear = 2025
    private var maxEnglishMonth = 4
    private var maxEnglishDay = 13

    private val baseEnglishDate = GregorianCalendar()
    private val maxEnglishDate = GregorianCalendar()

    init {
        // Index 1 is the first month
        nepaliYearToNumOfDaysInMonthMap[2075] = intArrayOf(0, 31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30)
        nepaliYearToNumOfDaysInMonthMap[2076] = intArrayOf(0, 31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30)
        nepaliYearToNumOfDaysInMonthMap[2077] = intArrayOf(0, 31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31)
        nepaliYearToNumOfDaysInMonthMap[2078] = intArrayOf(0, 31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30)
        nepaliYearToNumOfDaysInMonthMap[2079] = intArrayOf(0, 31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30)
        nepaliYearToNumOfDaysInMonthMap[2080] = intArrayOf(0, 31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30)
        nepaliYearToNumOfDaysInMonthMap[maxNepaliYear] = intArrayOf(0, 31, 31, 32, 32, 31, 30, 30, 30, 29, 30, 30, maxNepaliDay)

        // Below numbers might not be accurate
//        nepaliYearToNumOfDaysInMonthMap[2082] = intArrayOf(0, 30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30)
//        nepaliYearToNumOfDaysInMonthMap[2083] = intArrayOf(0, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30, 30)
//        nepaliYearToNumOfDaysInMonthMap[2084] = intArrayOf(0, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30, 30)
//        nepaliYearToNumOfDaysInMonthMap[2085] = intArrayOf(0, 31, 32, 31, 32, 30, 31, 30, 30, 29, 30, 30, 30)
//        nepaliYearToNumOfDaysInMonthMap[2086] = intArrayOf(0, 30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30)
//        nepaliYearToNumOfDaysInMonthMap[2087] = intArrayOf(0, 31, 31, 32, 31, 31, 31, 30, 30, 29, 30, 30, 30)
//        nepaliYearToNumOfDaysInMonthMap[2088] = intArrayOf(0, 30, 31, 32, 32, 30, 31, 30, 30, 29, 30, 30, 30)
//        nepaliYearToNumOfDaysInMonthMap[2089] = intArrayOf(0, 30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30)
//        nepaliYearToNumOfDaysInMonthMap[2090] = intArrayOf(0, 30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30)

        baseEnglishDate.setHumanReadableValues(baseEnglishYear, baseEnglishMonth, baseEnglishDay)
        maxEnglishDate.setHumanReadableValues(maxEnglishYear, maxEnglishMonth, maxEnglishDay)
    }

    @Throws(IndexOutOfBoundsException::class)
    fun convertToNepali(
        englishYear: Int,
        englishMonth: Int,
        englishDay: Int
    ): Triple<Int, Int, Int>
    {
        return convertToNepaliUsingHumanReadableValues(
            englishYear,
            englishMonth + 1,
            englishDay
        )
    }

    @Throws(IndexOutOfBoundsException::class)
    fun convertToNepaliUsingHumanReadableValues(
        englishYear: Int,
        englishMonth: Int,
        englishDay: Int
    ): Triple<Int, Int, Int>
    {
        // Why GreogorianCalendar instead of the usu Calendar.getInstance()?
        // Because if the locale is set to Japan then Calendar.getInstance()
        // will give you a JapaneseImperialCalendar or sth like that
        val englishDateToConvert = GregorianCalendar()
        englishDateToConvert.setHumanReadableValues(englishYear, englishMonth, englishDay)

        debugPrintCalendar("englishDateToConvert: ", englishDateToConvert)
        debugPrintCalendar("baseEnglishDate: ", baseEnglishDate)
        debugPrintCalendar("maxEnglishDate", maxEnglishDate)

        if (englishDateToConvert.beforeUpToDays(baseEnglishDate)) {
            throw IndexOutOfBoundsException("Date before day=$baseEnglishDay, month=$baseEnglishMonth, year=$baseEnglishYear not supported")
        } else if (maxEnglishDate.beforeUpToDays(englishDateToConvert)) {
            throw IndexOutOfBoundsException("Date after day=$maxEnglishDay, month=$maxEnglishMonth, year=$maxEnglishYear not supported")
        }

        var englishNumOfDaysSinceBase = numOfDaysBetween(baseEnglishDate, englishDateToConvert)
        debugPrint("convertToNepali: englishNumOfDaysSinceBase=$englishNumOfDaysSinceBase")

        var nepCurrentYear = baseNepaliYear
        var nepaliCurrentMonth = baseNepaliMonth
        var nepaliCurrentDay = baseNepaliDay

        debugPrint("convertToNepali: nepaliCurrentDay=$nepaliCurrentDay nepaliCurrentMonth=$nepaliCurrentMonth nepaliCurrentYear=$nepCurrentYear")

        // Decrement numOfDaysSinceBase until its value becomes 0
        while (englishNumOfDaysSinceBase != 0L) {

            // Get the total number of days in month, nepaliMonth, in year, nepaliYear.
            val nepNumOfDaysInMonthsForCurrentYear = nepaliYearToNumOfDaysInMonthMap[nepCurrentYear]
                ?: throw IndexOutOfBoundsException("Nepali Date with year=$nepCurrentYear is not supported.")

            val nepaliNumOfDaysInCurrentMonth = nepNumOfDaysInMonthsForCurrentYear[nepaliCurrentMonth]

            nepaliCurrentDay++
            englishNumOfDaysSinceBase--

            if (nepaliCurrentDay > nepaliNumOfDaysInCurrentMonth) {
                nepaliCurrentMonth++
                nepaliCurrentDay = 1
            }

            if (nepaliCurrentMonth > 12) {
                nepCurrentYear++
                nepaliCurrentMonth = 1
            }

            // Count the days in the increment of 7 days
            baseEnglishDayOfWeek++

            if (baseEnglishDayOfWeek > 7) {
                baseEnglishDayOfWeek = 1
            }
            debugPrint("convertToNepali: nepaliCurrentDay=$nepaliCurrentDay nepaliCurrentMonth=$nepaliCurrentMonth nepaliCurrentYear=$nepCurrentYear")
        }
        return Triple(nepCurrentYear, nepaliCurrentMonth, nepaliCurrentDay)
    }

    private val currentDate: Calendar = Calendar.getInstance()

    fun numOfDaysBetween(startDate: GregorianCalendar, endDate: GregorianCalendar): Long {

        debugPrintCalendar("baseDate", baseEnglishDate)
        debugPrintCalendar("startDate", startDate)
        debugPrintCalendar("endDate", endDate)

        currentDate.time = startDate.time
        var daysBetween: Long = 0

        debugPrintCalendar("currentDate", currentDate)

        while (currentDate.beforeUpToDays(endDate)) {
            currentDate.add(Calendar.DAY_OF_MONTH, 1)
            debugPrintCalendar("currentDate", currentDate)
            daysBetween++
        }

        return daysBetween
    }

    private fun debugPrintCalendar(prefix: String, calendar: Calendar) {
//        if (BuildConfig.DEBUG) {
//            Log.d(
//                "888", "logCalendar: $prefix " +
//                        "year=${calendar.get(Calendar.YEAR)} " +
//                        "month=${calendar.getHumanReadableValue(Calendar.MONTH)} " +
//                        "dayOfMonth=${calendar.getHumanReadableValue(Calendar.DAY_OF_MONTH)}"
//            )
//        }
    }

    private fun debugPrint(log: String) {
//        if (BuildConfig.DEBUG) {
//            Log.d("888", log)
//        }
    }

}