package com.keerthi77459.attendease.viewmodel

import android.database.sqlite.SQLiteDatabase

class FetchAttendanceData {
    fun fetchTrueValues(
        database: SQLiteDatabase,
        tableName: String,
        date: String,
        month: String,
        year: String,
        valueType: String
    ): List<Pair<String, List<String>>> {
        val tableNameQuery =
            "SELECT name FROM pragma_table_info('$tableName') WHERE name LIKE '_"+date+"_"+month+"_"+year+"_%';"
        val cursor = database.rawQuery(tableNameQuery, null)
        println(tableNameQuery)
        val resultData = mutableListOf<Pair<String, List<String>>>()

        if (cursor.moveToFirst()) {
            do {
                val columnName = cursor.getString(0)
                println(columnName)
                val trueQuery = "SELECT rollNo FROM $tableName WHERE $columnName = $valueType;"
                val trueCursor = database.rawQuery(trueQuery, null)

                val trueValues = mutableListOf<String>()
                if (trueCursor.moveToFirst()) {
                    do {
                        trueValues.add(trueCursor.getString(0))
                    } while (trueCursor.moveToNext())
                }
                trueCursor.close()

                resultData.add(columnName to trueValues)
            } while (cursor.moveToNext())
        }
        cursor.close()

        return resultData
    }
}