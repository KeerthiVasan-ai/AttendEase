package com.keerthi77459.attendease.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keerthi77459.attendease.R
import com.keerthi77459.attendease.adapter.ViewAttendanceAdapter
import com.keerthi77459.attendease.model.AttendanceData

@Suppress("DEPRECATION")
class ViewAttendanceList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_attendance_list)

        val attendanceType = intent.getSerializableExtra("attendanceType")
        val resultData =
            intent.getSerializableExtra("resultData") as? ArrayList<AttendanceData>

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = attendanceType?.toString()

        println(resultData)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ViewAttendanceAdapter(this, resultData ?: ArrayList())
    }
}