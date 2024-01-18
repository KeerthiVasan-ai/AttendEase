package com.keerthi77459.attendease.ui

import android.content.*
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.keerthi77459.attendease.R
import com.keerthi77459.attendease.db.DbHelper
import com.keerthi77459.attendease.model.ClassData
import com.keerthi77459.attendease.utils.Utils
import com.keerthi77459.attendease.viewmodel.Logic
import com.keerthi77459.attendease.viewmodel.MapRoll

class QuickAttendance : AppCompatActivity() {

    private lateinit var submitAttendance: Button
    private lateinit var rollNo: TextInputLayout
    private lateinit var lateralRollNo: TextInputLayout
    private lateinit var qaDegreeName: AutoCompleteTextView
    private lateinit var qaClassName: AutoCompleteTextView
    private lateinit var qaYearName: AutoCompleteTextView
    private lateinit var mappedRoll: MutableMap<String, String>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rollNoText: Array<String>
    private lateinit var lateralRollNoText: Array<String>
    private var qaDegreeText: String? = null
    private var qaClassText: String? = null
    private var qaYearText: String? = null
    lateinit var dbHelper: DbHelper
    private lateinit var builder: AlertDialog.Builder
    private lateinit var v: View
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_attendance)

        builder = AlertDialog.Builder(this)
        v = LayoutInflater.from(this).inflate(R.layout.fragement_alertbox, null)
        val classData = ClassData(this)
        val utils = Utils()
        val dbHelper = DbHelper(this)
        val logic = Logic(this)
        val mapRoll = MapRoll(this)
        mappedRoll = mapRoll.mapRoll()
        classData.getClass()

        qaDegreeName = findViewById(R.id.qaDegreeName)
        qaClassName = findViewById(R.id.qaClassName)
        qaYearName = findViewById(R.id.qaYearName)

        submitAttendance = findViewById(R.id.submitAttendance)
        rollNo = findViewById(R.id.rollNoField)
        lateralRollNo = findViewById(R.id.lateralRollNoField)

        val degreeAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.drop_down_text, classData.degreeName)
        val classAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.drop_down_text, classData.className)
        val semesterAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.drop_down_text, classData.yearName)

        qaYearName.setAdapter(semesterAdapter)
        qaClassName.setAdapter(classAdapter)
        qaDegreeName.setAdapter(degreeAdapter)

        qaYearName.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            qaYearText = qaYearName.text.toString()
        }

        qaClassName.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            qaClassText = qaClassName.text.toString()
        }

        qaDegreeName.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            qaDegreeText = qaDegreeName.text.toString()
        }

        submitAttendance.setOnClickListener {

            qaDegreeName.error = null
            qaClassName.error = null
            qaYearName.error = null

            sharedPreferences = this.getSharedPreferences("DoOnce", Context.MODE_PRIVATE)
            val lastRunTime: Long = sharedPreferences.getLong("LastRunTime", -1)
            val lastTableName: String = sharedPreferences.getString("LastTableName", "class")!!


            val isValid = validate(qaDegreeText, qaClassText, qaYearText)
            val tableName = qaDegreeText + "_" + qaClassText + "_" + qaYearText

            val isInitiated = logic.attendanceLogic(
                sharedPreferences,
                lastRunTime,
                lastTableName,
                tableName,
                utils.ATTENDANCE_INITAL_STATUS,
                utils.COLUMN_NAME
            )

            val db = dbHelper.writableDatabase


            rollNoText = rollNo.editText!!.text.split(",").toTypedArray()
            lateralRollNoText = lateralRollNo.editText!!.text.split(",").toTypedArray()

            if (isValid && (isInitiated == 1)) {

                if (rollNoText.isNotEmpty()) {
                    println("Empty")
                    for (rollNo in rollNoText) {
                        val className = "$qaDegreeText-$qaClassText-$qaYearText-R"
                        println(className)
                        val newRoll: String = when (rollNo.length) {
                            1 -> {
                                mappedRoll[className] + "00" + rollNo
                            }
                            2 -> {
                                mappedRoll[className] + "0" + rollNo
                            }
                            else -> {
                                mappedRoll[className] + rollNo
                            }
                        }
                        val contentValues1 = ContentValues()
                        contentValues1.put(utils.COLUMN_NAME, utils.ATTENDANCE_UPDATED_STATUS)
                        db.update(
                            tableName, contentValues1,
                            "rollNo=?",
                            arrayOf(newRoll)
                        )
                        println(newRoll)
                    }
                }

                if (lateralRollNoText.isNotEmpty()) {
                    for (lateralRoll in lateralRollNoText) {

                        val className = "$qaDegreeText-$qaClassText-$qaYearText-LE"
                        println(className)
                        val newRoll: String = when (lateralRoll.length) {
                            1 -> {
                                mappedRoll[className] + "00" + lateralRoll
                            }
                            2 -> {
                                mappedRoll[className] + "0" + lateralRoll
                            }
                            else -> {
                                mappedRoll[className] + lateralRoll
                            }
                        }
                        val contentValues1 = ContentValues()
                        contentValues1.put(utils.COLUMN_NAME, utils.ATTENDANCE_UPDATED_STATUS)
                        db.update(
                            tableName, contentValues1,
                            "rollNo=?",
                            arrayOf(newRoll)
                        )
                        println(newRoll)
                    }
                }

                Toast.makeText(this, "Attendance Submitted Successfully", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, MainActivity::class.java))

            } else {
                displayDialog(db, tableName, Utils().ATTENDANCE_UPDATE_WARNING)
                Toast.makeText(this, "Make New Attendance After 45 Minutes", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun validate(degreeText: String?, classText: String?, yearText: String?): Boolean {
        if (degreeText == null) {
            qaDegreeName.error = "Select a Degree"
            return false
        }
        if (classText == null) {
            qaClassName.error = "Enter the Class Name"
            return false
        }
        if (yearText == null) {
            qaYearName.error = "Select a Semester"
            return false
        }
        return true
    }

    private fun displayDialog(db: SQLiteDatabase, tableName: String, message: String) {

        val displayView: TextView = v.findViewById(R.id.alertbox)
        displayView.text = message
        builder.setView(v)
        builder.setTitle("WARNING")
            .setPositiveButton("I ,Understood") { _, _ ->
                val latestColumnName: String = sharedPreferences.getString("LatestColumn", null)!!

                if (rollNoText.isNotEmpty()) {
                    println("Empty")
                    for (rollNo in rollNoText) {
                        val className = "$qaDegreeText-$qaClassText-$qaYearText-R"
                        println(className)
                        val newRoll: String = when (rollNo.length) {
                            1 -> {
                                mappedRoll[className] + "00" + rollNo
                            }
                            2 -> {
                                mappedRoll[className] + "0" + rollNo
                            }
                            else -> {
                                mappedRoll[className] + rollNo
                            }
                        }
                        val contentValues1 = ContentValues()
                        contentValues1.put(latestColumnName, Utils().ATTENDANCE_UPDATED_STATUS)
                        db.update(
                            tableName, contentValues1,
                            "rollNo=?",
                            arrayOf(newRoll)
                        )
                        println(newRoll)
                    }
                }

                if (lateralRollNoText.isNotEmpty()) {
                    for (lateralRoll in lateralRollNoText) {

                        val className = "$qaDegreeText-$qaClassText-$qaYearText-LE"
                        println(className)
                        val newRoll: String = when (lateralRoll.length) {
                            1 -> {
                                mappedRoll[className] + "00" + lateralRoll
                            }
                            2 -> {
                                mappedRoll[className] + "0" + lateralRoll
                            }
                            else -> {
                                mappedRoll[className] + lateralRoll
                            }
                        }
                        val contentValues1 = ContentValues()
                        contentValues1.put(latestColumnName, Utils().ATTENDANCE_UPDATED_STATUS)
                        db.update(
                            tableName, contentValues1,
                            "rollNo=?",
                            arrayOf(newRoll)
                        )
                        println(newRoll)
                    }
                }

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
