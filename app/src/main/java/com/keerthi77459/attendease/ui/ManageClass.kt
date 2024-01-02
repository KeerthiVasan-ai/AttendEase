package com.keerthi77459.attendease.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keerthi77459.attendease.R
import com.keerthi77459.attendease.adapter.ManageClassAdapter
import com.keerthi77459.attendease.model.ClassData

class ManageClass : AppCompatActivity() {

    lateinit var recycle3 : RecyclerView
    lateinit var manageClassAdapter: ManageClassAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_class)

        val classData = ClassData(this)

        recycle3 = findViewById(R.id.recycle3)
        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Manage Class"

        classData.getClass()
        manageClassAdapter = ManageClassAdapter(this,classData.degreeName,classData.className,classData.yearName)
        recycle3.adapter = manageClassAdapter
        recycle3.layoutManager = LinearLayoutManager(this)
    }
}