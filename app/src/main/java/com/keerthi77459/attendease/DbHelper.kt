package com.keerthi77459.attendease

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

open class DbHelper(context: Context) : SQLiteOpenHelper(context,DB_NAME,null,DB_VERSION){

    companion object{
        private var DB_VERSION = 1
        private const val DB_NAME = "AttendEase.db"
        protected const val TABLE_STUDENT_DETAIL = "studentDetail"
        protected const val TABLE_ATTENDANCE_DETAIL = "attendanceDetail"
        protected const val TABLE_CLASS_DETAIL = "classDetail"

    }
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_STUDENT_DETAIL(" +
                "rollNo TEXT PRIMARY KEY,name TEXT,degree TEXT,class TEXT,year INTEGER,phoneNumber TEXT)")

        db?.execSQL("CREATE TABLE $TABLE_CLASS_DETAIL("+
                "degree TEXT,class TEXT,year TEXT)")

        db?.execSQL("CREATE TABLE $TABLE_ATTENDANCE_DETAIL(" +
                "rollNo TEXT PRIMARY KEY)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
}