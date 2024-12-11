package com.keerthi77459.attendease.viewmodel

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.keerthi77459.attendease.utils.Utils

class QuickAttendanceLogic(context: Context) {

    private lateinit var mappedRoll: MutableMap<String, String>
    private val mapRoll = MapRoll(context)
    fun quickAttendance(
        db: SQLiteDatabase,
        utils: Utils,
        tableName: String,
        rollNoList: Array<String>,
        columnName: String,
        lateralRollNoList: Array<String>,
        actualAttendanceState: String
    ) {
        mappedRoll = mapRoll.mapRoll()
        if (rollNoList.isNotEmpty()) {
            for (rollNo in rollNoList) {
                val className = "${tableName.replace("_", "-")}-R"
                println(className)
                val newRoll: String = when (rollNo.length) {
                    1 -> {
                        if (mappedRoll[className]!!.trim().isEmpty()) {
                            mappedRoll[className]!!.trim() + rollNo
                        } else {
                            mappedRoll[className] + "00" + rollNo

                        }
                    }

                    2 -> {
                        if (mappedRoll[className]!!.trim().isEmpty()) {
                            mappedRoll[className]!!.trim() + rollNo
                        } else {
                            mappedRoll[className] + "0" + rollNo
                        }
                    }

                    else -> {
                        mappedRoll[className] + rollNo
                    }
                }
                val contentValues1 = ContentValues()
                Log.d("TESTING", newRoll)
                contentValues1.put(columnName, actualAttendanceState)
                db.update(
                    tableName, contentValues1,
                    "rollNo=?",
                    arrayOf(newRoll)
                )
                println(newRoll)
            }
        }

        if (lateralRollNoList.isNotEmpty()) {
            for (lateralRoll in lateralRollNoList) {
                val className = "${tableName.replace("_", "-")}-LE"
                println(className)
                val newRoll: String = when (lateralRoll.length) {
                    1 -> {
                        mappedRoll[className] + "00" + lateralRoll
                    }

                    2 -> {
                        mappedRoll[className] + "0" + lateralRoll
                    }

                    else -> {
                        mappedRoll[className] + lateralRoll
                    }
                }
                val contentValues1 = ContentValues()
                contentValues1.put(columnName, actualAttendanceState)
                db.update(
                    tableName, contentValues1,
                    "rollNo=?",
                    arrayOf(newRoll)
                )
                println(newRoll)
            }
        }
    }
}