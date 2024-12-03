package com.keerthi77459.attendease.utils

import android.content.res.Resources
import com.keerthi77459.attendease.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

open class Utils {

//    val resources: Resources = Resources.getSystem()

    val DB_NAME = "AttendEase.db"
    val DB_VERSION = 1
    val TABLE_CLASS_DETAIL = "classDetail"

    val TIME_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")
    val CALENDER: Calendar = Calendar.getInstance(TIME_ZONE)
    val CURRENT_YEAR: Int = CALENDER.get(Calendar.YEAR)
    val CURRENT_DATE: Int = CALENDER.get(Calendar.DATE)
    val CURRENT_MONTH: Int = CALENDER.get(Calendar.MONTH) + 1
    var TIMESTAMP: String =
        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).split(".")[0].replace(":", "_")

    val COLUMN_NAME =
        "_" + returnDate(CURRENT_DATE.toString()) + "_" + CURRENT_MONTH.toString() + "_" + CURRENT_YEAR.toString() + "_" + TIMESTAMP

    var CURRENT_TIME: Long = System.currentTimeMillis()

    //    TODO: CHANGE THE COMPARISON CONSTANT ACCORDING TO THE TIME FROM ONBOARDING SCREEN
    var COMPARISON_CONSTANT: Long = 45 * 60 * 1000

    var ATTENDANCE_PRESENT: String = "1"
    var ATTENDANCE_ABSENT: String = "0"

    var ADD_CLASS_MESSAGE = "Welcome to Add Class Section. \n" +
            "In this section, you can create a class. \n" +
            "For that, you need to enter : \n" +
            "- Degree \n" +
            "- Programme \n" +
            "- Semester \n" +
            "Following by the Excel sheet (both .xlsx and .xls) with the columns of: \n" +
            "- Roll No \n" +
            "- Student Name \n" +
            "- Phone Number \n" +
            "-Mode of Study - R or LE \n"
    var ATTENDANCE_UPDATE_WARNING = "Attention \n" +
            "You are editing previous response \n" +
            "You can make a new attendance only after 45 minutes \n" +
            "CLICK OK TO PROCEED"

    fun returnDate(date: String): String {
        if (date.length == 1) {
            return "0$date"
        }
        return date
    }

    fun getAttendanceStatus(resources: Resources, attendanceType: String): Pair<String?, String?> {
        var initialAttendanceState: String? = null
        var actualAttendanceState: String? = null
        when (attendanceType) {
//            Absent

            resources.getStringArray(R.array.attendance_type)[0] -> {
                initialAttendanceState = ATTENDANCE_PRESENT
                actualAttendanceState = ATTENDANCE_ABSENT
            }

//             Present
            resources.getStringArray(R.array.attendance_type)[1] -> {
                initialAttendanceState = ATTENDANCE_ABSENT
                actualAttendanceState = ATTENDANCE_PRESENT
            }
        }
        return Pair(initialAttendanceState, actualAttendanceState)
    }

    fun mapComparisonConstant() {

    }

    fun insertToCloud() {

    }
}