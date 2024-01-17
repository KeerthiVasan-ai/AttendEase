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
}