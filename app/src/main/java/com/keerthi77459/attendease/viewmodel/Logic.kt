package com.keerthi77459.attendease.viewmodel

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import com.keerthi77459.attendease.db.DbHelper
import com.keerthi77459.attendease.utils.Utils

class Logic(context: Context) {
    private val dbHelper = DbHelper(context)
    private val utils = Utils()

    fun attendanceLogic(sharedPreferences: SharedPreferences,
                        lastRunTime: Long,
                        lastTableName: String,
                        tableName: String,
                        initialAttendanceState:String,
                        columnName : String,
                        isModify : Boolean):Int
    {
        if(((utils.CURRENT_TIME - lastRunTime) > utils.COMPARISON_CONSTANT) || (lastTableName != tableName)){

            val db = dbHelper.writableDatabase

            val alterQuery = "ALTER TABLE $tableName ADD COLUMN $columnName TEXT DEFAULT NULL"
            db.execSQL(alterQuery)

            val contentValues = ContentValues()
            contentValues.put(columnName,initialAttendanceState)
            db.update(tableName,contentValues,null,null)

            if(isModify) {
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putLong("LastRunTime", utils.CURRENT_TIME)
                editor.putString("LastTableName", tableName)
                editor.putString("LatestColumn", columnName)
                editor.apply()
            }

            return 1
        } else {
            return 0
        }
    }
}