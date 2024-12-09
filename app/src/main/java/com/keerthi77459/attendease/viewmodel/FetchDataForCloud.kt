package com.keerthi77459.attendease.viewmodel

import android.database.sqlite.SQLiteDatabase
import com.keerthi77459.attendease.model.CloudData

class FetchDataForCloud {
    fun fetchDataForCloudInsertion(
        db: SQLiteDatabase,
        tableName: String,
        columnName: String
    ): CloudData {
        var presentCount = 0
        var absentCount = 0
        val absenteesName = mutableMapOf<String, List<Map<String, String>>>()

        val presentQuery = "SELECT COUNT(*) FROM $tableName WHERE `$columnName` = 1"
        val absentQuery = "SELECT COUNT(*) FROM $tableName WHERE `$columnName` = 0"

        val absenteesQuery = """
            SELECT rollNo, name, mode 
            FROM $tableName 
            WHERE `$columnName` = 0
        """.trimIndent()

        val presentQueryCursor = db.rawQuery(presentQuery, null)
        if (presentQueryCursor.moveToFirst()) {
            presentCount = presentQueryCursor.getInt(0)
        }
        presentQueryCursor.close()

        val absentQueryCursor = db.rawQuery(absentQuery, null)
        if (absentQueryCursor.moveToFirst()) {
            absentCount = absentQueryCursor.getInt(0)
        }
        absentQueryCursor.close()

        val absenteesCursor = db.rawQuery(absenteesQuery, null)
        val modeMap = mutableMapOf<String, MutableList<Map<String, String>>>()
        while (absenteesCursor.moveToNext()) {
            val rollNo = absenteesCursor.getString(absenteesCursor.getColumnIndexOrThrow("rollNo"))
            val name = absenteesCursor.getString(absenteesCursor.getColumnIndexOrThrow("name"))
            val mode = absenteesCursor.getString(absenteesCursor.getColumnIndexOrThrow("mode"))

            val absentee = mapOf("rollNo" to rollNo, "name" to name)
            modeMap.computeIfAbsent(mode) { mutableListOf() }.add(absentee)
        }
        absenteesCursor.close()

        modeMap.forEach { (mode, absentees) ->
            absenteesName[mode] = absentees.toList()
        }

        return CloudData(presentCount.toString(), absentCount.toString(), absenteesName)
    }
}
