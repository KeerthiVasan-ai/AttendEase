package com.keerthi77459.attendease.cloud

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class GetDepartmentNamesFromCloud {

    fun getDepartmentNames(institutionId: String, callback: DataFetch) {

        val departments = mutableListOf<String>()
        val db = FirebaseFirestore.getInstance()

        val reference = db.collection(institutionId).get()

        reference.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val querySnapshot = task.result
                if (querySnapshot != null) {
                    for (department in querySnapshot) {
                        departments.add(department.id)
                    }
                    if (departments.isNotEmpty()) {
                        callback.onSuccess(departments.toTypedArray())
                    } else {
                        callback.onFailure("Check your Insitution Id")

                    }
                } else {
                    callback.onFailure("Check your Insitution Id")
                }
            } else {
                callback.onFailure("Check your Insitution Id")
            }
        }
    }
}