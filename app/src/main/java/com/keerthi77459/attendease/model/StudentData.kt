package com.keerthi77459.attendease.model

import android.content.Context
import android.database.Cursor
import com.keerthi77459.attendease.db.DbHelper

class StudentData(context:Context) {

    lateinit var rollNo: ArrayList<String>
    lateinit var name: ArrayList<String>
    lateinit var phoneNumber: ArrayList<String>
    private val dbHelper = DbHelper(context)

     fun getStudentDetails(tableName : String):Int {
        println(tableName)

        rollNo = ArrayList()
        name = ArrayList()
        phoneNumber = ArrayList()
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM $tableName"
        val cursor: Cursor = db.rawQuery(query, null)
        if (cursor.count == 0) {
            return 0
        } else {
            while (cursor.moveToNext()) {
                rollNo.add(cursor.getString(0))
                name.add(cursor.getString(1))
                phoneNumber.add(cursor.getString(5))
            }
        }
        cursor.close()
        return 1
    }
}