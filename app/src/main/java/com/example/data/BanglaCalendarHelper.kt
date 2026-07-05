package com.example.data

import java.util.Calendar

data class BanglaDate(
    val day: Int,
    val monthName: String,
    val year: Int,
    val seasonName: String
)

object BanglaCalendarHelper {

    private val banglaMonths = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )

    private val banglaSeasons = listOf(
        "গ্রীষ্মকাল", "বর্ষাকাল", "শরৎকাল", "হেমন্তকাল", "শীতকাল", "বসন্তকাল"
    )

    /**
     * Converts a Gregorian Calendar to the official Bangladeshi Bangla Date (বঙ্গাব্দ).
     * Reformed Bangla Calendar rules (Bangladesh Standard):
     * Boishakh to Bhadro (First 5 months) = 31 days each.
     * Ashwin to Chaitra (Next 7 months) = 30 days each, EXCEPT Falgun which is 31 days in Gregorian leap years.
     * Bangla New Year (1 Boishakh) starts on April 14.
     */
    fun getBanglaDate(calendar: Calendar): BanglaDate {
        val gYear = calendar.get(Calendar.YEAR)
        val gMonth = calendar.get(Calendar.MONTH) // 0-indexed (Jan = 0, Apr = 3)
        val gDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Check if Gregorian year is a leap year
        val isLeapYear = (gYear % 4 == 0 && gYear % 100 != 0) || (gYear % 400 == 0)

        // Calculate day of year in Gregorian (1 to 365 or 366)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        // April 14 is generally Day 104 in non-leap year (31+28+31+14) and Day 105 in leap year
        val boishakhStartDay = if (isLeapYear) 105 else 104

        val bYear: Int
        var daysElapsed: Int

        if (dayOfYear < boishakhStartDay) {
            // Before April 14 -> Belongs to previous Bangla Year
            bYear = gYear - 594
            // Days remaining from previous year's Boishakh 1
            val prevYearLeap = ((gYear - 1) % 4 == 0 && (gYear - 1) % 100 != 0) || ((gYear - 1) % 400 == 0)
            val prevYearTotalDays = if (prevYearLeap) 366 else 365
            val prevBoishakhStart = if (prevYearLeap) 105 else 104
            daysElapsed = (prevYearTotalDays - prevBoishakhStart) + dayOfYear
        } else {
            // After or on April 14 -> Current Bangla Year
            bYear = gYear - 593
            daysElapsed = dayOfYear - boishakhStartDay
        }

        // Days in each Bangla month according to reformed rules
        val monthDays = mutableListOf(
            31, 31, 31, 31, 31, // বৈশাখ, জ্যৈষ্ঠ, আষাঢ়, শ্রাবণ, ভাদ্র (৩১ দিন)
            30, 30, 30, 30, 30, // আশ্বিন, কার্তিক, অগ্রহায়ণ, পৌষ, মাঘ (৩০ দিন)
            if (isLeapYear && dayOfYear >= boishakhStartDay) 31 else 30, // ফাল্গুন (লীপ ইয়ারে ৩১ দিন, নতুবা ৩০ দিন)
            30 // চৈত্র (৩০ দিন)
        )

        var mIdx = 0
        var remDays = daysElapsed
        while (mIdx < 12 && remDays >= monthDays[mIdx]) {
            remDays -= monthDays[mIdx]
            mIdx++
        }

        val bDay = remDays + 1
        val finalMonthIdx = mIdx.coerceIn(0, 11)
        val bMonth = banglaMonths[finalMonthIdx]
        val season = banglaSeasons[finalMonthIdx / 2]

        return BanglaDate(bDay, bMonth, bYear, season)
    }
}
