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
        var TIMESTAMP :String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).split(".")[0].replace(":","_")
        val COLUMN_NAME = "_" + CURRENT_DATE.toString() + "_" + CURRENT_MONTH.toString() + "_" + TIMESTAMP
        var CURRENT_TIME: Long = System.currentTimeMillis()
        var COMPARISON_CONSTANT: Long = 45 * 60 * 1000

        var ATTENDANCE_INITAL_STATUS : String = "1"
        var ATTENDANCE_UPDATED_STATUS : String = "0"

        var ADD_CLASS_MESSAGE = "Welcome to Add Class Section. \n"+
                "In this section, you can create a class. \n"+
                "For that, you need to enter : \n"+
                "- Degree \n"+
                "- Programme \n"+
                "- Semester \n"+
                "Following by the Excel sheet (both .xlsx and .xls) with the columns of: \n"+
                "- Roll No \n"+
                "- Student Name \n"+
                "- Phone Number \n"+
                "-Mode of Study - R or LE \n"
        var ATTENDANCE_UPDATE_WARNING = "Attention \n"+
                "You are editing previous response \n"+
                "You can make a new attendance only after 45 minutes \n"+
                "CLICK OK TO PROCEED"
}