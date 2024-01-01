package com.keerthi77459.attendease.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

open class Utils {

        val DB_NAME= "AttendEase.db"
        val DB_VERSION = 1
        val TABLE_STUDENT_DETAIL = "studentDetail"
        val TABLE_ATTENDANCE_DETAIL = "attendanceDetail"
        val TABLE_CLASS_DETAIL = "classDetail"

        val TIME_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")
        val CALENDER: Calendar = Calendar.getInstance(TIME_ZONE)
        val CURRENT_DAY: Int = CALENDER.get(Calendar.DAY_OF_YEAR)
        val CURRENT_DATE: Int = CALENDER.get(Calendar.DATE)
        val CURRENT_MONTH: Int = CALENDER.get(Calendar.MONTH) + 1
        val COLUMN_NAME = "_" + CURRENT_DATE.toString() + "_" + CURRENT_MONTH.toString()
        var TIMESTAMP :String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).split(".")[0]

        var ATTENDANCE_INITAL_STATUS : String = "1"
        var ATTENDANCE_UPDATED_STATUS : String = "0"
}