package com.keerthi77459.attendease.ui

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.keerthi77459.attendease.R
import com.keerthi77459.attendease.viewmodel.Backup

class BackupScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_screen)

        val btnBackup = findViewById<Button>(R.id.btnBackup)
        val btnRestore = findViewById<Button>(R.id.btnRestore)
        val backup = Backup()

        btnBackup.setOnClickListener {
            backup.backupDatabase(this)
        }

        btnRestore.setOnClickListener {
            backup.restoreDatabase(this)
        }


    }
}