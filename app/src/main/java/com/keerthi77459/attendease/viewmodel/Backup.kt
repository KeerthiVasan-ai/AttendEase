package com.keerthi77459.attendease.viewmodel

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import com.keerthi77459.attendease.utils.Utils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class Backup {

    fun backupDatabase(context: Context) {
        val currentDBPath = context.getDatabasePath(Utils().DB_NAME).absolutePath
        Log.d("Backup", "Current DB path: $currentDBPath")

        val backupDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val folder = File("$backupDir/AttendEase", "Backup")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val fileName = "AttendEase_Backup"
        val backupPath: File?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 12,13,14
            val downloadDir = Environment.getExternalStorageDirectory().path
            backupPath = File("$downloadDir/Download/AttendEase/Backup", fileName)
        } else {
            // 9,10,11
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            backupPath = File(File("$downloadsDir/AttendEase", "Backup"), fileName)
        }

        Log.d("Backup", "Backup DB path: ${backupPath.absolutePath}")

        try {
            val src = FileInputStream(currentDBPath).channel
            val dst = FileOutputStream(backupPath).channel

            dst.transferFrom(src, 0, src.size())

            src.close()
            dst.close()

            Log.d("Backup", "Backup completed successfully")
        } catch (e: IOException) {
            Log.e("Backup", "Error backing up database: ${e.message}")
        }
    }


    fun restoreDatabase(context: Context) {
        val fileName = "AttendEase_Backup"
        val backupDir = File(Environment.getExternalStorageDirectory().path, "Download/AttendEase/Backup")
        println(backupDir)
        val backupDBPath = File(backupDir, fileName)

        if (backupDBPath.exists()) {
            val currentDBPath = context.getDatabasePath(Utils().DB_NAME).absolutePath
            Log.d("Restore", "Current DB path: $currentDBPath")

            try {
                // Close any open connections before restoring
                val dbFile = File(currentDBPath)
                if (dbFile.exists()) {
                    dbFile.delete()
                }

                backupDir.mkdirs() // Ensure directories exist
                val src = FileInputStream(backupDBPath).channel
                val dst = FileOutputStream(currentDBPath).channel

                dst.transferFrom(src, 0, src.size())

                src.close()
                dst.close()

                Log.d("Restore", "Database restored successfully")
            } catch (e: IOException) {
                Log.e("Restore", "Error restoring database: ${e.message}")
            }
        } else {
            Log.e("Restore", "Backup file not found")
        }
    }


}