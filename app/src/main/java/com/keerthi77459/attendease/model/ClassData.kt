package com.keerthi77459.attendease.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.keerthi77459.attendease.db.DbHelper
import com.keerthi77459.attendease.utils.Utils

class ClassData(context: Context) {

    lateinit var className: ArrayList<String>
    lateinit var degreeName: ArrayList<String>
    lateinit var yearName: ArrayList<String>
    private val dbHelper = DbHelper(context)
    private val utils = Utils()

    fun getClass() : Int {
        className = ArrayList()
        degreeName = ArrayList()
        yearName = ArrayList()
        val db = dbHelper.writableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${utils.TABLE_CLASS_DETAIL}", null)
        return if (cursor.count == 0) {
            0
        } else {
            while (cursor.moveToNext()) {
                degreeName.add(cursor.getString(0))
                className.add(cursor.getString(1))
                yearName.add(cursor.getString(2))
            }
            1
        }
    }

    fun addClass(degreeText: String, classText: String, yearText: String): Long {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val query = "SELECT * FROM ${utils.TABLE_CLASS_DETAIL} WHERE degree = '$degreeText' AND class = '$classText' AND year = '$yearText'"
        val cursor = db.rawQuery(query, null)
        println(cursor.count)
        return if (cursor.count != 0) {
            0
        } else {
            val result: Long
            val contentValue1 = ContentValues()
            contentValue1.put("degree", degreeText)
            contentValue1.put("class", classText)
            contentValue1.put("year", yearText)
            result = db.insert(utils.TABLE_CLASS_DETAIL, null, contentValue1)
            result
        }
    }
}