package com.keerthi77459.attendease.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.keerthi77459.attendease.utils.Utils

open class DbHelper(context: Context) : SQLiteOpenHelper(context, Utils().DB_NAME,null, Utils().DB_VERSION){

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE ${Utils().TABLE_CLASS_DETAIL}("+
                "degree TEXT,class TEXT,year TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    fun fetchAttendanceDetails(tableName: String): String {
        val columnsToExclude = arrayOf("name", "degree", "class", "year", "phoneNumber", "mode")
        return "SELECT ${buildIncludedColumns(columnsToExclude, tableName)} FROM $tableName"
    }


    private fun buildIncludedColumns(columnsToExclude: Array<String>, tableName:String): String {
        val cursor = readableDatabase.rawQuery("PRAGMA table_info($tableName)",null)
        val includedColumn = StringBuilder()
        while (cursor.moveToNext()){
            val nameIndex = cursor.getColumnIndex("name")
            if (nameIndex > 0) {
                val columnName = cursor.getString(nameIndex)
                if (!contain(columnsToExclude, columnName)) {
                    if (includedColumn.isNotEmpty()) {
                        includedColumn.append(",")
                    }
                    includedColumn.append(columnName)
                }
            }
        }
        cursor.close()
        return includedColumn.toString()
    }

    private fun contain(columnsToExclude: Array<String>, columnName: String?): Boolean {
        return columnsToExclude.any {it == columnName}
    }
}