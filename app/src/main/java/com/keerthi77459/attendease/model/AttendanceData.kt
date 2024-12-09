package com.keerthi77459.attendease.model

import java.io.Serializable

data class AttendanceData(
    val columnName: String,
    val attendanceDataBasedOnGroup: Map<String, List<String>>
) : Serializable