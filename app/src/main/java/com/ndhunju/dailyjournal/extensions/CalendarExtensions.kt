package com.ndhunju.dailyjournal.extensions

import java.util.*

fun Calendar.setHumanReadableValues(year: Int, month: Int, day: Int) {
    // Calendar considers 0 for month as first month
    set(year, month - 1, day)
}

fun Calendar.getHumanReadableValue(field: Int): Int {

    if (field == Calendar.MONTH) {
        // Calendar considers 0 for month as first month
        return get(field) + 1
    }

    return get(field)
}

/**
 * Like [Calendar.before] but this one only checks year, month and day.
 * It ignores hour, minute, seconds and so on
 */
fun Calendar.beforeUpToDays(calendar: Calendar): Boolean {

    val dayOfYear1 = this[Calendar.DAY_OF_YEAR]
    val monthOfYear1 = this[Calendar.MONTH]
    val year1 = this[Calendar.YEAR]

    val dayOfYear2 = calendar[Calendar.DAY_OF_YEAR]
    val monthOfYear2 = calendar[Calendar.MONTH]
    val year2 = calendar[Calendar.YEAR]

    return if (year1 == year2) {
        // Years are equal, check months
        if (monthOfYear1 == monthOfYear2) {
            // Months are equal, check day
            if (dayOfYear1 == dayOfYear2) {
                false
            } else dayOfYear1 < dayOfYear2
        } else monthOfYear1 < monthOfYear2
    } else year1 < year2

}