package com.keerthi77459.attendease.viewmodel

import android.content.Context
import android.database.Cursor
import com.keerthi77459.attendease.db.DbHelper
import com.keerthi77459.attendease.utils.Utils


class MapRoll(context: Context) {

    private var rollMapping = mutableMapOf<String, String>()
    private val dbHelper = DbHelper(context)
    private lateinit var classNames: ArrayList<String>
    private lateinit var rollNumbers: ArrayList<String>
    private lateinit var lateralRollNumbers: ArrayList<String>

    fun mapRoll(): MutableMap<String, String> {

        getRollDetails()
        println(classNames)
        println(rollNumbers)
        println(lateralRollNumbers)

        for (index in classNames.indices) {
            rollMapping.put(classNames[index].plus("-R"), rollNumbers[index])
            rollMapping.put(classNames[index].plus("-LE"), lateralRollNumbers[index])
        }

        println(rollMapping)
        return rollMapping
    }

    private fun getRollDetails(): Int {
        classNames = ArrayList()
        rollNumbers = ArrayList()
        lateralRollNumbers = ArrayList()

        val db = dbHelper.writableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${Utils().TABLE_CLASS_DETAIL}", null)
        return if (cursor.count == 0) {
            0
        } else {
            while (cursor.moveToNext()) {
                val tableName =
                    cursor.getString(1) + "_" + cursor.getString(2) + "_" + cursor.getString(3) + "_" + cursor.getString(4)
                val rollQuery = "SELECT rollNo FROM $tableName WHERE mode = 'R'"
                val lateralQuery = "SELECT rollNo FROM $tableName WHERE mode = 'LE'"

                val rollCursor = db.rawQuery(rollQuery, null)
                val lateralCursor = db.rawQuery(lateralQuery, null)
                if (rollCursor.moveToFirst()) {
                    rollNumbers.add(rollCursor.getString(0).toString().dropLast(3))
                    rollCursor.close()
                }
                if (lateralCursor.moveToFirst()) {
                    lateralRollNumbers.add(lateralCursor.getString(0).toString().dropLast(3))
                    lateralCursor.close()
                }


                classNames.add(
                    cursor.getString(1).toString() + "-" + cursor.getString(2)
                        .toString() + "-" + cursor.getString(3).toString()
                )
            }

            cursor.close()
            1
        }
    }
}