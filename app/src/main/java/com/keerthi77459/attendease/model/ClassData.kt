package com.keerthi77459.attendease.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.keerthi77459.attendease.db.DbHelper
import com.keerthi77459.attendease.utils.Utils

class ClassData(context: Context) {

    lateinit var departmentName: ArrayList<String>
    lateinit var className: ArrayList<String>
    lateinit var degreeName: ArrayList<String>
    lateinit var yearName: ArrayList<String>
    lateinit var classType: ArrayList<String>
    lateinit var classStrength: ArrayList<String>
    private lateinit var mergedDetails: ArrayList<String>
    private val dbHelper = DbHelper(context)
    private val utils = Utils()

    fun getClass(): Int {
        departmentName = ArrayList()
        className = ArrayList()
        degreeName = ArrayList()
        yearName = ArrayList()
        classType = ArrayList()
        classStrength = ArrayList()
        val db = dbHelper.writableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${utils.TABLE_CLASS_DETAIL}", null)
        return if (cursor.count == 0) {
            cursor.close()
            0
        } else {
            while (cursor.moveToNext()) {
                departmentName.add(cursor.getString(0))
                degreeName.add(cursor.getString(1))
                className.add(cursor.getString(2))
                yearName.add(cursor.getString(3))
                classType.add(cursor.getString(4))
                classStrength.add(cursor.getString(5))
            }
            cursor.close()
            1
        }
    }

    fun mergedClassDetails(): ArrayList<String> {
        getClass()
        mergedDetails = ArrayList()
        for (i in 0 until degreeName.size) {
            mergedDetails.add(degreeName[i] + "-" + className[i] + "-" + yearName[i] + "-" + classType[i])
        }
        return mergedDetails
    }

    fun addClass(
        departmentText: String,
        degreeText: String,
        classText: String,
        yearText: String,
        classTypeText: String,
        strength: String
    ): Long {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val query =
            "SELECT * FROM ${utils.TABLE_CLASS_DETAIL} WHERE degree = '$degreeText' AND class = '$classText' AND year = '$yearText'"
        val cursor = db.rawQuery(query, null)
        println(cursor.count)
        return if (cursor.count != 0) {
            cursor.close()
            0
        } else {
            val result: Long
            val contentValue1 = ContentValues()
            contentValue1.put("department", departmentText)
            contentValue1.put("degree", degreeText)
            contentValue1.put("class", classText)
            contentValue1.put("year", yearText)
            contentValue1.put("class_type", classTypeText)
            contentValue1.put("strength", strength)
            result = db.insert(utils.TABLE_CLASS_DETAIL, null, contentValue1)
            cursor.close()

            result
        }
    }
}