package com.keerthi77459.attendease.cloud

import com.keerthi77459.attendease.model.CloudData
import com.google.firebase.firestore.FirebaseFirestore

class AttendanceDataToCloud {
    fun insertAttendanceData(
        institutionId: String,
        departmentName: String,
        classType: String,
        className: String,
        columName: String,
        cloudData: CloudData
    ) {
        val firestore = FirebaseFirestore.getInstance()

        val attendanceData = hashMapOf(
            "presentCount" to cloudData.presentCount,
            "absentCount" to cloudData.absentCount,
            "absenteesDetails" to cloudData.absenteesDetails
        )

        firestore.collection(institutionId).document(departmentName).collection(classType)
            .document(className).collection("attendance")
            .document(columName)
            .set(attendanceData)
            .addOnSuccessListener {
                println("Attendance data successfully inserted with document: $columName")
            }
            .addOnFailureListener { exception ->
                println("Error inserting attendance data: ${exception.message}")
            }
    }

    fun updateAttendanceData(
        institutionId: String,
        departmentName: String,
        classType: String,
        className: String,
        columnName: String,
        cloudData: CloudData
    ) {
        val firestore = FirebaseFirestore.getInstance()

        val attendanceData = hashMapOf(
            "presentCount" to cloudData.presentCount,
            "absentCount" to cloudData.absentCount,
            "absentees_details" to cloudData.absenteesDetails
        )

        firestore.collection(institutionId).document(departmentName).collection(classType)
            .document(className).collection("attendance")
            .document(columnName)
            .update(attendanceData as Map<String, Any>)
            .addOnSuccessListener {
                println("Attendance data successfully updated for document: $columnName")
            }
            .addOnFailureListener { exception ->
                println("Error updating attendance data: ${exception.message}")
            }
    }
}
