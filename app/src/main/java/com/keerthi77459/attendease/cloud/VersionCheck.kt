package com.keerthi77459.attendease.cloud

import com.google.firebase.firestore.FirebaseFirestore

class VersionCheck {

    fun checkVersion(callback: (Pair<String, Boolean>?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("app_details").document("data").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val data = task.result.data
                    if (data != null) {
                        val version: String = data["version"] as String
                        val isAppUnderMaintenance = data["isAppUnderMaintenance"] as Boolean

                        callback(Pair(version, isAppUnderMaintenance))
                        return@addOnCompleteListener
                    }
                }
                callback(null)
            }
    }
}
