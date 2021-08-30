package com.yaya.apod.util

import java.text.SimpleDateFormat
import java.util.*

class Util {

    companion object {

        fun getTodayDate(): String {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CANADA)
            val calendar = Calendar.getInstance()
            return simpleDateFormat.format(calendar.time);
        }

        fun getDateBeforeToday(daysCountBeforeToday: Int): String {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CANADA)
            val calendar = Calendar.getInstance()

            calendar.add(Calendar.DAY_OF_YEAR, -daysCountBeforeToday)
            return simpleDateFormat.format(Date(calendar.timeInMillis))
        }

        fun getDateAfterToday(daysCountAfterToday: Int): String {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CANADA)
            val calendar = Calendar.getInstance()

            calendar.add(Calendar.DAY_OF_YEAR, daysCountAfterToday)
            return simpleDateFormat.format(Date(calendar.timeInMillis))
        }


    }
}