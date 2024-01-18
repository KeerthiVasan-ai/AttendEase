package com.keerthi77459.attendease.ui

import android.content.*
import android.database.sqlite.SQLiteDatabase
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
import java.util.*

class StudentDetail : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbHelper: DbHelper
    private lateinit var recycler2: RecyclerView
    private lateinit var studentDetailAdapter: StudentDetailAdapter
    private lateinit var absentButton: Button
    private lateinit var switchMaterial: SwitchMaterial
    private lateinit var tableName : String
    private var attendanceInitialMode: String = Utils().ATTENDANCE_INITAL_STATUS   //1
    private var attendanceUpdatedMode: String = Utils().ATTENDANCE_UPDATED_STATUS  //0
    private lateinit var absentNumber: ArrayList<String>
    private lateinit var builder: AlertDialog.Builder
    private lateinit var v: View
    private lateinit var dialog : AlertDialog


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

        absentNumber = ArrayList()
        absentButton = findViewById(R.id.absent)

        sharedPreferences = this.getSharedPreferences("dataPassing", Context.MODE_PRIVATE)
        val outDegreeName: String = sharedPreferences.getString("outDegreeName", null)!!
        val outClassName: String = sharedPreferences.getString("outClassName", null)!!
        val outYearName: String = sharedPreferences.getString("outYearName", null)!!

        tableName = outDegreeName + "_" + outClassName + "_" + outYearName

        val isFetched = studentData.getStudentDetails(tableName)
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
                val lastTableName: String = sharedPreferences.getString("LastTableName","class")!!

                val isInitiated = logic.attendanceLogic(
                    sharedPreferences,
                    lastRunTime,
                    lastTableName,
                    tableName,
                    attendanceInitialMode,
                    utils.COLUMN_NAME
                )
                absentNumber = studentDetailAdapter.attendedRoll
                println(absentNumber)

                if (isInitiated == 1) {

                    for (absent in absentNumber) {
                        val contentValues1 = ContentValues()
                        contentValues1.put(utils.COLUMN_NAME, attendanceUpdatedMode)
                        db.update(
                            tableName, contentValues1,
                            "rollNo=?",
                            arrayOf(absent)
                        )
                    }
                    println(absentNumber)
                    Toast.makeText(this, "Attendance Submitted Successfully", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, MainActivity::class.java))

                } else {
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
            .setPositiveButton("I ,Understood") { _, _ ->
                val latestColumnName: String = sharedPreferences.getString("LatestColumn", null)!!
                absentNumber = studentDetailAdapter.attendedRoll

                for (absent in absentNumber) {
                    val contentValues1 = ContentValues()
                    contentValues1.put(latestColumnName, attendanceUpdatedMode)
                    db.update(
                        tableName, contentValues1,
                        "rollNo=?",
                        arrayOf(absent)
                    )
                }
                println(absentNumber)
                Toast.makeText(this, "Attendance Updated", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, MainActivity::class.java))

            }.setNegativeButton("Cancel") { _, _ ->
                dialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
            }
        dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
}
