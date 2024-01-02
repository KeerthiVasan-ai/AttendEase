package com.keerthi77459.attendease.viewmodel

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import com.keerthi77459.attendease.db.DbHelper
import com.keerthi77459.attendease.utils.Utils

class Logic(context: Context) {
    private val dbHelper = DbHelper(context)
    private val utils = Utils()

    fun attendanceLogic(sharedPreferences: SharedPreferences,lastRunTime: Long,attendanceState:String,
                        columnName : String,
                        outDegreeName: String,
                        outClassName: String,
                        outYearName: String):Int
    {
        if((Utils().CURRENT_TIME - lastRunTime) > utils.COMPARISON_CONSTANT ){

            val db = dbHelper.writableDatabase

            val alterQuery = "ALTER TABLE ${utils.TABLE_ATTENDANCE_DETAIL} ADD COLUMN $columnName TEXT DEFAULT NULL"
            db.execSQL(alterQuery)

            val contentValues = ContentValues()
            contentValues.put(columnName,attendanceState)
            val whereClause = "rollNo IN (SELECT rollNo FROM studentDetail WHERE degree = '$outDegreeName' AND class = '$outClassName' AND year = '$outYearName')"
            db.update(utils.TABLE_ATTENDANCE_DETAIL,contentValues,whereClause,null)

            val editor : SharedPreferences.Editor = sharedPreferences.edit()
            editor.putLong("LastRunTime",utils.CURRENT_TIME)
            editor.putString("LatestColumn",columnName)
            editor.apply()

            return 1
        } else {
            return 0
        }
    }
}