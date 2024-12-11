package com.keerthi77459.attendease.ui

import android.annotation.SuppressLint
import android.content.*
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import com.keerthi77459.attendease.cloud.AttendanceDataToCloud
import com.keerthi77459.attendease.db.DbHelper
import com.keerthi77459.attendease.viewmodel.Logic
import com.keerthi77459.attendease.model.StudentData
import com.keerthi77459.attendease.utils.Utils
import com.keerthi77459.attendease.viewmodel.FetchDataForCloud
import java.util.*

class StudentDetail : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbHelper: DbHelper
    private lateinit var recycler2: RecyclerView
    private lateinit var studentDetailAdapter: StudentDetailAdapter
    private lateinit var timeSlot: AutoCompleteTextView
    private lateinit var absentButton: Button
    private lateinit var switchMaterial: SwitchMaterial
    private lateinit var tableName: String
    private lateinit var columnName: String
    private var timeText: String? = null
    private var initialAttendanceState: String = Utils().ATTENDANCE_PRESENT   //1
    private var actualAttendanceState: String = Utils().ATTENDANCE_ABSENT  //0
    private lateinit var absentNumber: ArrayList<String>
    private lateinit var builder: AlertDialog.Builder
    private lateinit var v: View
    private lateinit var dialog: AlertDialog


    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Student Details"

        builder = AlertDialog.Builder(this)
        v = LayoutInflater.from(this).inflate(R.layout.fragement_alertbox, null)
        dbHelper = DbHelper(this)
        val studentData = StudentData(this)
        val utils = Utils()
        val logic = Logic(this)

        absentNumber = ArrayList()
        absentButton = findViewById(R.id.absent)
        absentButton.isEnabled = false
        timeSlot = findViewById(R.id.timeSlotName)

        sharedPreferences = this.getSharedPreferences("dataPassing", Context.MODE_PRIVATE)
        val outDegreeName: String = sharedPreferences.getString("outDegreeName", null)!!
        val outClassName: String = sharedPreferences.getString("outClassName", null)!!
        val outYearName: String = sharedPreferences.getString("outYearName", null)!!
        val classType: String = sharedPreferences.getString("classType", null)!!

        tableName = outDegreeName + "_" + outClassName + "_" + outYearName + "_" + classType

        val theorySlotAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            R.layout.drop_down_text,
            resources.getStringArray(R.array.theoryClassSlots)
        )

        val labSlotAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            R.layout.drop_down_text,
            resources.getStringArray(R.array.labClassSlots)
        )

        if (classType == resources.getStringArray(R.array.class_type)[0]) {
            timeSlot.setAdapter(theorySlotAdapter)
        } else if (classType == resources.getStringArray(R.array.class_type)[1]) {
            timeSlot.setAdapter(labSlotAdapter)
        }

        timeSlot.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            switchMaterial.isEnabled = true
            timeText = timeSlot.text.toString()

            columnName = utils.getColumnName(time = timeText!!)

            updateCheckboxesBasedOnHour(columnName, actualAttendanceState)
        }

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
            disableCheckboxes()

        } else {
            Toast.makeText(this, "No Students", Toast.LENGTH_LONG).show()
            absentButton.isEnabled = false
        }

        absentButton.setOnClickListener {

            val db: SQLiteDatabase = dbHelper.writableDatabase

            absentNumber = studentDetailAdapter.attendedRoll


            val totalStudents = studentData.rollNo.size
            val absentees: Int
            val presentees: Int

            if (switchMaterial.isChecked) {
                presentees = absentNumber.size
                absentees = totalStudents - presentees
            } else {
                absentees = absentNumber.size
                presentees = totalStudents - absentees
            }

            val attendanceSummary = "Total Students: $totalStudents\n" +
                    "Absentees: $absentees\n" +
                    "Presentees: $presentees"

            val displayView: TextView = v.findViewById(R.id.alertbox)
            displayView.text = attendanceSummary
            builder.setView(v)
            builder.setTitle("WARNING")
                .setPositiveButton("I ,Understood") { _, _ ->
                    displayDialog(
                        db,
                        logic,
                        columnName,
                        utils,
                        outDegreeName,
                        outClassName,
                        outYearName,
                        classType
                    )
                }.setNegativeButton("CANCEL") { _, _ ->
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

        }
    }

    private fun disableCheckboxes() {
        for (i in 0 until studentDetailAdapter.itemCount) {
            val holder =
                recycler2.findViewHolderForAdapterPosition(i) as? StudentDetailAdapter.StudentDetailViewHolder
            holder?.isAttended?.isEnabled = false
        }
    }

    private fun enableCheckboxes() {
        for (i in 0 until studentDetailAdapter.itemCount) {
            val holder =
                recycler2.findViewHolderForAdapterPosition(i) as? StudentDetailAdapter.StudentDetailViewHolder
            holder?.isAttended?.isEnabled = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.switch_menu, menu)
        val itemSwitch = menu?.findItem(R.id.toggle)
        itemSwitch?.setActionView(R.layout.toggle_layout)

        switchMaterial = menu?.findItem(R.id.toggle)?.actionView!!.findViewById(R.id.toogleRoot)
        switchMaterial.isEnabled = false

        switchMaterial.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                initialAttendanceState = Utils().ATTENDANCE_ABSENT
                actualAttendanceState = Utils().ATTENDANCE_PRESENT

                updateCheckboxesBasedOnHour(columnName, actualAttendanceState)

                Toast.makeText(this, "You are going to select Present Students", Toast.LENGTH_SHORT)
                    .show()
            } else {
                initialAttendanceState = Utils().ATTENDANCE_PRESENT
                actualAttendanceState = Utils().ATTENDANCE_ABSENT
                updateCheckboxesBasedOnHour(columnName, actualAttendanceState)
            }
        }
        return true
    }

    private fun displayDialog(
        db: SQLiteDatabase,
        logic: Logic,
        columnName: String,
        utils: Utils,
        outDegreeName: String,
        outClassName: String,
        outYearName: String,
        classType: String
    ) {
        val isInitiated = logic.attendanceLogic(
            tableName,
            initialAttendanceState,
            columnName,
        )
        if (isInitiated == 1) {

            for (absent in absentNumber) {
                val contentValues1 = ContentValues()
                contentValues1.put(columnName, actualAttendanceState)
                db.update(
                    tableName, contentValues1,
                    "rollNo=?",
                    arrayOf(absent)
                )
            }

            val institutionId = utils.getInstitutionId(this)
            val departmentName = utils.getDepartmentName(db, tableName)
            val cloudClassName = "$outDegreeName-$outClassName-$outYearName"

            val cloudData =
                FetchDataForCloud().fetchDataForCloudInsertion(
                    db,
                    tableName,
                    columnName
                )

            AttendanceDataToCloud().insertAttendanceData(
                institutionId!!,
                departmentName,
                classType,
                cloudClassName,
                columnName,
                cloudData
            )

            println(absentNumber)
            Toast.makeText(this, "Attendance Submitted Successfully", Toast.LENGTH_LONG)
                .show()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        } else {

            for (absent in absentNumber) {
                val contentValues1 = ContentValues()
                contentValues1.put(columnName, actualAttendanceState)
                db.update(
                    tableName, contentValues1,
                    "rollNo=?",
                    arrayOf(absent)
                )
            }

            val institutionId = utils.getInstitutionId(this)
            val departmentName = utils.getDepartmentName(db, tableName)
            val cloudClassName = "$outDegreeName-$outClassName-$outYearName"

            val cloudData =
                FetchDataForCloud().fetchDataForCloudInsertion(db, tableName, columnName)

            AttendanceDataToCloud().insertAttendanceData(
                institutionId!!,
                departmentName,
                classType,
                cloudClassName,
                columnName,
                cloudData
            )

            Toast.makeText(this, "Attendance Updated", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun updateCheckboxesBasedOnHour(columnName: String, actualAttendanceState: String) {
        val db = dbHelper.readableDatabase
        val attendanceMap = mutableMapOf<String, Boolean>()

        try {
            val queryCursor = db.rawQuery("SELECT rollNo, $columnName FROM $tableName", null)

            while (queryCursor.moveToNext()) {
                val rollNo = queryCursor.getString(0)
                val attendanceState = queryCursor.getString(1)
                attendanceMap[rollNo] = attendanceState == actualAttendanceState
            }
            queryCursor.close()
        } catch (e: Exception) {
            enableCheckboxes()
            absentButton.isEnabled = true
        }

        val attendedRollList = mutableListOf<String>()
        for (rollNo in attendanceMap.keys) {
            if (attendanceMap[rollNo] == true) {
                attendedRollList.add(rollNo)
            }
        }
        studentDetailAdapter.updateAttendance(attendedRollList)

        absentButton.isEnabled = true
    }

}
