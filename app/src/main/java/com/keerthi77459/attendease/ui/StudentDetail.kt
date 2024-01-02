package com.keerthi77459.attendease.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.keerthi77459.attendease.viewmodel.AlertDialogBox
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
    private lateinit var builder: AlertDialog.Builder
    private lateinit var v: View
    private lateinit var dialog : AlertDialog

//    NOTE : IF ATTENDANCE MODE = 1 : CHECKBOX WILL BE CONSIDERED AS PRESENT

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Student Details"

        builder = AlertDialog.Builder(this)
        v = LayoutInflater.from(this).inflate(R.layout.fragement_alertbox,null)
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

                val isInitiated = logic.attendanceLogic(
                    sharedPreferences,
                    lastRunTime,
                    attendanceInitialMode,
                    utils.COLUMN_NAME,
                    outDegreeName,
                    outClassName,
                    outYearName
                )
                absentNumber = studentDetailAdapter.attendedRoll

                if (isInitiated == 1) {

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
                    displayDialog(db,Utils().ATTENDANCE_UPDATE_WARNING)
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
    private fun displayDialog(db:SQLiteDatabase,message:String){

        val displayView : TextView = v.findViewById(R.id.alertbox)
        displayView.text = message
        builder.setView(v)
        builder.setTitle("WARNING")
            .setPositiveButton("I ,Understood", DialogInterface.OnClickListener { _, _ ->
                val latestColumnName :String = sharedPreferences.getString("LatestColumn",null)!!
                absentNumber = studentDetailAdapter.attendedRoll

                for (absent in absentNumber) {
                    val contentValues1 = ContentValues()
                    contentValues1.put(latestColumnName, attendanceUpdatedMode)
                    db.update(
                        "attendanceDetail", contentValues1,
                        "rollNo=?",
                        arrayOf(absent)
                    )
                }
                Toast.makeText(this, "Attendance Updated", Toast.LENGTH_LONG).show()
            }).setNegativeButton("Cancel",DialogInterface.OnClickListener{_,_->
                dialog.dismiss()
            })
        dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
}
