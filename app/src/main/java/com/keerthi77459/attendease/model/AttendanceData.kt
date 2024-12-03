package com.keerthi77459.attendease.model

import java.io.Serializable

data class AttendanceData(
    val columnName: String,
    val trueValues: List<String>
) : Serializable