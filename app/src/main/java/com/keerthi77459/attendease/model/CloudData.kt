package com.keerthi77459.attendease.model

data class CloudData(
    var presentCount: String,
    var absentCount: String,
    var absenteesDetails: Map<String, List<Map<String, String>>>
)