package com.keerthi77459.attendease.utils

import android.content.res.Resources
import com.keerthi77459.attendease.R
import com.keerthi77459.attendease.viewmodel.MapDateAndTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

open class Utils {

    val DB_NAME = "AttendEase.db"
    val DB_VERSION = 1
    val TABLE_CLASS_DETAIL = "classDetail"

    val DATE =
        java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

    var TIMESTAMP: String =
        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).split(".")[0].replace(":", "_")

    var ATTENDANCE_PRESENT: String = "1"
    var ATTENDANCE_ABSENT: String = "0"

    var ADD_CLASS_MESSAGE = "Welcome to Add Class Section. \n" +
            "In this section, you can create a class. \n" +
            "For that, you need to enter : \n" +
            "- Department \n" +
            "Other fields like  \n" +
            "- Class Type \n - Class Name " +
            "- Strength will be populated automatically \n" +
            "Along with this, you need to input the Excel sheet (both .xlsx and .xls) with the columns of: \n" +
            "- Roll No \n" +
            "- Student Name \n" +
            "- Mode of Study - R or LE \n"
    var ATTENDANCE_UPDATE_WARNING = "Attention \n" +
            "You are editing previous response \n" +
            "CLICK OK TO PROCEED"


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

    fun getColumnName(date: String = DATE, time: String): String {
        return "_${date.split(" ")[0]}_${MapDateAndTime().timeDate[date.split(" ")[1]]}_${
            date.split(" ")[2]
        }_${time.split(":")[0]}_${time.split(":")[1]}_00"
    }

    fun insertToCloud() {

    }
}


