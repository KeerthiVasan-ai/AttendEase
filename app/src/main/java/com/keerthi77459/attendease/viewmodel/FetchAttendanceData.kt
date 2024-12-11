package com.keerthi77459.attendease.viewmodel

import android.database.sqlite.SQLiteDatabase

class FetchAttendanceDataNew {
    fun fetchTrueValuesGroupedByColumnAndMode(
        database: SQLiteDatabase,
        tableName: String,
        date: String,
        month: String,
        year: String,
        valueType: String
    ): Map<String, Map<String, List<String>>> {

        val tableNameQuery =
            "SELECT name FROM pragma_table_info('$tableName') WHERE name LIKE '_${date}_${month}_${year}_%';"
        val cursor = database.rawQuery(tableNameQuery, null)
        println(tableNameQuery)

        val resultData = mutableMapOf<String, MutableMap<String, MutableList<String>>>()

        if (cursor.moveToFirst()) {
            do {
                val columnName = cursor.getString(0)
                val columnData = resultData.computeIfAbsent(columnName) { mutableMapOf() }

                val groupedQuery = """
                    SELECT mode, rollNo 
                    FROM $tableName 
                    WHERE $columnName = $valueType 
                    GROUP BY mode, rollNo
                    ORDER BY mode DESC;
                """.trimIndent()
                val groupCursor = database.rawQuery(groupedQuery, null)

                if (groupCursor.moveToFirst()) {
                    do {
                        val mode = groupCursor.getString(0)
                        val rollNo = groupCursor.getString(1)
                        println(rollNo)
                        val clippedRoll = rollNo.takeLast(3).replace("^0".toRegex(), "")
                        columnData.computeIfAbsent(mode) { mutableListOf() }.add(clippedRoll)
                    } while (groupCursor.moveToNext())
                }
                groupCursor.close()
            } while (cursor.moveToNext())
        }
        cursor.close()

        // Convert mutable maps to immutable for final output
        return resultData.mapValues { columnEntry ->
            columnEntry.value.mapValues { modeEntry ->
                modeEntry.value.toList()
            }
        }
    }
}

