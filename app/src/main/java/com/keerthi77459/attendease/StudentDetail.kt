package com.keerthi77459.attendease

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import kotlin.collections.ArrayList

class StudentDetail : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbHelper: DbHelper
    lateinit var name: ArrayList<String>
    lateinit var rollNo: ArrayList<String>
    lateinit var phoneNumber: ArrayList<String>
    lateinit var recycler2: RecyclerView
    lateinit var studentDetailAdapter: StudentDetailAdapter
    lateinit var absentButton : Button
    lateinit var absentNumber : ArrayList<String>

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)

        dbHelper = DbHelper(this)
        name = ArrayList()
        rollNo = ArrayList()
        phoneNumber = ArrayList()
        absentNumber = ArrayList()
        absentButton = findViewById(R.id.absent)

        sharedPreferences = this.getSharedPreferences("dataPassing", Context.MODE_PRIVATE)
        var outDegreeName: String = sharedPreferences.getString("outDegreeName", null)!!
        var outClassName: String = sharedPreferences.getString("outClassName", null)!!
        var outYearName: String = sharedPreferences.getString("outYearName", null)!!
        getStudentDetails(outDegreeName, outClassName, outYearName)

        recycler2 = findViewById(R.id.recycle2)
        studentDetailAdapter = StudentDetailAdapter(this, rollNo, name, phoneNumber)
        recycler2.adapter = studentDetailAdapter
        recycler2.layoutManager = LinearLayoutManager(this)

        absentButton.setOnClickListener {
            val timeZone : TimeZone = TimeZone.getTimeZone("Asia/Kolkata")
            val calendar : Calendar = Calendar.getInstance(timeZone)
            val currentDay : Int = calendar.get(Calendar.DAY_OF_YEAR)
            val currentDate : Int = calendar.get(Calendar.DATE)
            val currentMonth : Int = calendar.get(Calendar.MONTH) + 1
            val columnName = "_"+currentDate.toString()+"_"+currentMonth.toString()
            val db : SQLiteDatabase = dbHelper.writableDatabase

            sharedPreferences = this.getSharedPreferences("DoOnce", Context.MODE_PRIVATE)
            val lastDay : Int = sharedPreferences.getInt("LastRunStudentDay",-1)

            if(lastDay != currentDay){
                println(lastDay)
                val contentValues  = ContentValues()
                contentValues.put(columnName,"1")

                val whereClause = "rollNo IN (SELECT rollNo FROM studentDetail WHERE degree = '$outDegreeName' AND class = '$outClassName' AND year = '$outYearName')"
                db.update("attendanceDetail",contentValues,whereClause,null)

                val editor : SharedPreferences.Editor = sharedPreferences.edit()
                editor.putInt("LastRunStudentDay",currentDay)
                editor.apply()
            }
            absentNumber = studentDetailAdapter.getAttendedRoll()
            for(absent in absentNumber){
                val contentValues1  = ContentValues()
                contentValues1.put(columnName,"0")
                db.update("attendanceDetail",contentValues1,
                    "rollNo=?",
                    arrayOf(absent)
                )
            }

            println(absentNumber)
            Toast.makeText(this,"Attendance Submitted Successfully",Toast.LENGTH_LONG).show()
        }
    }

    private fun getStudentDetails(degreeName: String, className: String, yearName: String) {
        println(degreeName)
        println(className)
        println(yearName)
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM studentDetail WHERE degree = '$degreeName' AND class = '$className' AND year = '$yearName'"
        val cursor: Cursor = db.rawQuery(query, null)
        if (cursor.count == 0) {
            Toast.makeText(this, "No Students", Toast.LENGTH_SHORT).show()
        } else {
            while (cursor.moveToNext()) {
                rollNo.add(cursor.getString(0))
                name.add(cursor.getString(1))
                phoneNumber.add(cursor.getString(5))
            }
        }
    }
}