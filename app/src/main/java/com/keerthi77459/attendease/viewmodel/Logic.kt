package com.keerthi77459.attendease.viewmodel

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.openOrCreateDatabase
import android.util.Log
import com.keerthi77459.attendease.db.DbHelper
import com.keerthi77459.attendease.utils.Utils

class Logic(context: Context) {
    private val dbHelper = DbHelper(context)
    private val utils = Utils()

    fun attendanceLogic(sharedPreferences: SharedPreferences,
                        lastRunTime: Long,
                        lastTableName: String,
                        tableName: String,
                        attendanceState:String,
                        columnName : String):Int
    {
        if(((Utils().CURRENT_TIME - lastRunTime) > utils.COMPARISON_CONSTANT) || (lastTableName != tableName)){

            val db = dbHelper.writableDatabase

            val alterQuery = "ALTER TABLE $tableName ADD COLUMN $columnName TEXT DEFAULT NULL"
            db.execSQL(alterQuery)

            val contentValues = ContentValues()
            contentValues.put(columnName,attendanceState)
            db.update(tableName,contentValues,null,null)

            val editor : SharedPreferences.Editor = sharedPreferences.edit()
            editor.putLong("LastRunTime",utils.CURRENT_TIME)
            editor.putString("LastTableName",tableName)
            editor.putString("LatestColumn",columnName)
            editor.apply()

            return 1
        } else {
            return 0
        }
    }
}