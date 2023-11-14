package com.keerthi77459.attendease

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.util.Calendar
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    lateinit var recycler1: RecyclerView
    lateinit var dbHelper: DbHelper
    lateinit var classAdapter: ClassAdapter
    lateinit var className: ArrayList<String>
    lateinit var degreeName : ArrayList<String>
    lateinit var yearName : ArrayList<String>
    lateinit var sharedPreferences: SharedPreferences
    lateinit var addClass : FloatingActionButton
    lateinit var quickAttendance : FloatingActionButton
    lateinit var generateReport : FloatingActionButton
    lateinit var menuStartPlus : FloatingActionButton
    lateinit var addClassText : TextView
    lateinit var quickAttendanceText : TextView
    lateinit var generateReportText : TextView
    var isVisible : Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DbHelper(this)
        className = ArrayList<String>()
        degreeName = ArrayList<String>()
        yearName = ArrayList<String>()
        getClass()

        addClass = findViewById(R.id.addClass)
        quickAttendance = findViewById(R.id.quickAttendance1)
        generateReport = findViewById(R.id.generateReport)
        menuStartPlus = findViewById(R.id.menuStartPlus)
        addClassText = findViewById(R.id.addClassText)
        quickAttendanceText = findViewById(R.id.quickAttendanceText1)
        generateReportText = findViewById(R.id.generateReportText)

        addClass.visibility = View.GONE
        quickAttendance.visibility = View.GONE
        generateReport.visibility = View.GONE
        addClassText.visibility = View.GONE
        quickAttendanceText.visibility = View.GONE
        generateReportText.visibility = View.GONE

        isVisible = false

        recycler1 = findViewById(R.id.recycle1)
        classAdapter = ClassAdapter(this,degreeName,className,yearName)
        recycler1.adapter = classAdapter
        recycler1.layoutManager = LinearLayoutManager(this)

        doOnce()

        menuStartPlus.setOnClickListener {
            isVisible = if (!isVisible!!) {

                addClass.show()
                quickAttendance.show()
                generateReport.show()
                addClassText.visibility = View.VISIBLE
                quickAttendanceText.visibility = View.VISIBLE
                generateReportText.visibility = View.VISIBLE
                true

            } else {
                addClass.hide()
                quickAttendance.hide()
                generateReport.hide()
                addClassText.visibility = View.GONE
                quickAttendanceText.visibility = View.GONE
                generateReportText.visibility = View.GONE
                false
            }
        }

        addClass.setOnClickListener {
            startActivity(Intent(this,AddClass::class.java))
        }

        generateReport.setOnClickListener{
            startActivity(Intent(this,GenerateReport::class.java))
        }

        quickAttendance.setOnClickListener {
            startActivity(Intent(this,QuickAttendance::class.java))
        }


    }

    private fun getClass() {
        val db = dbHelper.writableDatabase
        var cursor: Cursor = db.rawQuery("SELECT * FROM classDetail", null)
        if (cursor.count == 0) {
            Toast.makeText(this, "No Class Created", Toast.LENGTH_SHORT).show()
        } else {
            while (cursor.moveToNext()) {
                degreeName.add(cursor.getString(0))
                className.add(cursor.getString(1))
                yearName.add(cursor.getString(2))
            }
        }

    }

    private fun doOnce(){
        val timeZone : TimeZone = TimeZone.getTimeZone("Asia/Kolkata")
        val calendar : Calendar = Calendar.getInstance(timeZone)
        val currentDay : Int = calendar.get(Calendar.DAY_OF_YEAR)
        val currentDate : Int = calendar.get(Calendar.DATE)
        val currentMonth : Int = calendar.get(Calendar.MONTH) + 1
        val columnName = "_"+currentDate.toString()+"_"+currentMonth.toString()
        println(columnName)

        sharedPreferences = this.getSharedPreferences("DoOnce", Context.MODE_PRIVATE)
        val lastDay : Int = sharedPreferences.getInt("LastRunClassDay",-1)
        if(lastDay != currentDay){

            val db : SQLiteDatabase = dbHelper.writableDatabase
            val alterQuery : String = "ALTER TABLE attendanceDetail ADD COLUMN $columnName TEXT DEFAULT NULL"
            db.execSQL(alterQuery)
            db.close()

            val editor : SharedPreferences.Editor = sharedPreferences.edit()
            editor.putInt("LastRunClassDay",currentDay)
            editor.apply()
        }
    }
}
