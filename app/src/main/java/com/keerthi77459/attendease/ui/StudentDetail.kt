package com.keerthi77459.attendease.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.keerthi77459.attendease.R
import com.keerthi77459.attendease.adapter.StudentDetailAdapter
import com.keerthi77459.attendease.db.DbHelper
import com.keerthi77459.attendease.viewmodel.Logic
import com.keerthi77459.attendease.model.StudentData
import com.keerthi77459.attendease.utils.Utils
import java.util.*

class StudentDetail : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbHelper: DbHelper
    lateinit var recycler2: RecyclerView
    lateinit var studentDetailAdapter: StudentDetailAdapter
    lateinit var absentButton: Button
    lateinit var switchMaterial: SwitchMaterial
    var attendanceInitialMode: String = Utils().ATTENDANCE_INITAL_STATUS
    var attendanceUpdatedMode: String = Utils().ATTENDANCE_UPDATED_STATUS
    lateinit var absentNumber: ArrayList<String>

//    NOTE : IF ATTENDANCE MODE = 1 : CHECKBOX WILL BE CONSIDERED AS PRESENT

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Student Details"

        dbHelper = DbHelper(this)
        val studentData = StudentData(this)
        val utils = Utils()
        val logic = Logic(this)

//        logic.initialLogic(sharedPreferences, lastDay)
        println(utils.COLUMN_NAME)
        absentNumber = ArrayList()
        absentButton = findViewById(R.id.absent)

        sharedPreferences = this.getSharedPreferences("dataPassing", Context.MODE_PRIVATE)
        val outDegreeName: String = sharedPreferences.getString("outDegreeName", null)!!
        val outClassName: String = sharedPreferences.getString("outClassName", null)!!
        val outYearName: String = sharedPreferences.getString("outYearName", null)!!
        val isFetched = studentData.getStudentDetails(outDegreeName, outClassName, outYearName)
        if (isFetched == 1) {

            recycler2 = findViewById(R.id.recycle2)
            studentDetailAdapter =
                StudentDetailAdapter(
                    this,
                    studentData.rollNo,
                    studentData.name,
                    studentData.phoneNumber
                )
            recycler2.adapter = studentDetailAdapter
            recycler2.layoutManager = LinearLayoutManager(this)

            absentButton.setOnClickListener {

                val db: SQLiteDatabase = dbHelper.writableDatabase

                sharedPreferences = this.getSharedPreferences("DoOnce", Context.MODE_PRIVATE)
                val lastRunTime: Long = sharedPreferences.getLong("LastRunTime", -1)
                println("Saved run Time$lastRunTime")
                val isInitiated = logic.attendanceLogic(
                    sharedPreferences,
                    lastRunTime,
                    attendanceInitialMode,
                    utils.COLUMN_NAME,
                    outDegreeName,
                    outClassName,
                    outYearName
                )
                if (isInitiated == 1) {

                    absentNumber = studentDetailAdapter.attendedRoll

                    for (absent in absentNumber) {
                        val contentValues1 = ContentValues()
                        contentValues1.put(utils.COLUMN_NAME, attendanceUpdatedMode)
                        db.update(
                            "attendanceDetail", contentValues1,
                            "rollNo=?",
                            arrayOf(absent)
                        )
                    }
                    println(absentNumber)
                    Toast.makeText(this, "Attendance Submitted Successfully", Toast.LENGTH_LONG).show()
                } else {
//                    TODO CREATE A ALERT MESSAGE
                    Toast.makeText(this, "Make New Attendance After 45 Minutes", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "No Students", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.switch_menu, menu)
        val itemSwitch = menu?.findItem(R.id.toggle)
        itemSwitch?.setActionView(R.layout.toggle_layout)

        switchMaterial = menu?.findItem(R.id.toggle)?.actionView!!.findViewById(R.id.toogleRoot)

        switchMaterial.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                attendanceInitialMode = Utils().ATTENDANCE_UPDATED_STATUS
                attendanceUpdatedMode = Utils().ATTENDANCE_INITAL_STATUS
                Toast.makeText(this, "You are going to select Present Students ", Toast.LENGTH_LONG)
                    .show()
            }
        }
        return true
    }
}

//                sharedPreferences = this.getSharedPreferences("DoOnce", Context.MODE_PRIVATE)
//                val lastRunClassTime: Long = sharedPreferences.getLong("LastRunClassTime", -1)
//
//                logic.initialLogic2(sharedPreferences,lastRunClassTime)
//                print("COLUMN CREATED")
//                println(utils.COLUMN_NAME)
////                sharedPreferences = this.getSharedPreferences("DoOnce", Context.MODE_PRIVATE)
////                val lastDay: Int = sharedPreferences.getInt("LastRunStudentDay", -1)
//
//                sharedPreferences = this.getSharedPreferences("DoOnce", Context.MODE_PRIVATE)
//                val lastRunStudentTime: Long = sharedPreferences.getLong("LastRunStudentTime", -1)
//
//                logic.specificLogic2(
//                    sharedPreferences,
//                    lastRunStudentTime,
//                    attendanceInitialMode,
//                    outDegreeName,
//                    outClassName,
//                    outYearName
//                )
