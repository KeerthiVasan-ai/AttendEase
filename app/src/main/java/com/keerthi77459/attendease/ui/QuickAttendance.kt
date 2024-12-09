package com.keerthi77459.attendease.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.keerthi77459.attendease.R
import com.keerthi77459.attendease.cloud.AttendanceDataToCloud
import com.keerthi77459.attendease.db.DbHelper
import com.keerthi77459.attendease.model.ClassData
import com.keerthi77459.attendease.utils.Utils
import com.keerthi77459.attendease.viewmodel.FetchDataForCloud
import com.keerthi77459.attendease.viewmodel.Logic
import com.keerthi77459.attendease.viewmodel.QuickAttendanceLogic

class QuickAttendance : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var submitAttendance: Button
    private lateinit var rollNo: TextInputLayout
    private lateinit var lateralRollNo: TextInputLayout

    private lateinit var qaDegreeName: AutoCompleteTextView
    private lateinit var qaTimeName: AutoCompleteTextView
    private lateinit var qaAttendanceTypeName: AutoCompleteTextView

    private lateinit var rollNoText: Array<String>
    private lateinit var lateralRollNoText: Array<String>
    private lateinit var overallClassDetails: ArrayList<String>

    private lateinit var qaLogic: QuickAttendanceLogic

    private var qaDegreeText: String? = null
    private var qaClassText: String? = null
    private var qaYearText: String? = null
    private var qaClassTypeText: String? = null
    private var qaTimeText: String? = null
    private var qaAttendanceType: String? = null

    private lateinit var builder: AlertDialog.Builder
    private lateinit var v: View
    private lateinit var dialog: AlertDialog

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_attendance)

        builder = AlertDialog.Builder(this)
        v = LayoutInflater.from(this).inflate(R.layout.fragement_alertbox, null)

        val classData = ClassData(this)
        val utils = Utils()
        val dbHelper = DbHelper(this)
        qaLogic = QuickAttendanceLogic(this)
        val logic = Logic(this)

        overallClassDetails = classData.mergedClassDetails()

        qaDegreeName = findViewById(R.id.qaDegreeName)
        qaTimeName = findViewById(R.id.qaTimeName)
        qaAttendanceTypeName = findViewById(R.id.qaAttendanceTypeName)
        submitAttendance = findViewById(R.id.submitAttendance)
        rollNo = findViewById(R.id.rollNoField)
        lateralRollNo = findViewById(R.id.lateralRollNoField)

        val degreeAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.drop_down_text, overallClassDetails)

        val attendanceTypeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this, R.layout.drop_down_text, resources.getStringArray(R.array.attendance_type)
        )

        qaDegreeName.setAdapter(degreeAdapter)
        qaAttendanceTypeName.setAdapter(attendanceTypeAdapter)

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

        qaDegreeName.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            qaDegreeText = qaDegreeName.text.toString().split("-")[0]
            qaClassText = qaDegreeName.text.toString().split("-")[1]
            qaYearText = qaDegreeName.text.toString().split("-")[2]
            qaClassTypeText = qaDegreeName.text.toString().split("-")[3]

            if (qaClassTypeText == resources.getStringArray(R.array.class_type)[0]) {
                qaTimeName.setAdapter(theorySlotAdapter)
            } else if (qaClassTypeText == resources.getStringArray(R.array.class_type)[1]) {
                qaTimeName.setAdapter(labSlotAdapter)
            }
        }

        qaTimeName.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            qaTimeText = qaTimeName.text.toString()
        }

        qaAttendanceTypeName.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            qaAttendanceType = qaAttendanceTypeName.text.toString()
        }

        submitAttendance.setOnClickListener {

            qaDegreeName.error = null
            qaTimeName.error = null
            qaAttendanceTypeName.error = null

            val columnName = utils.getColumnName(time = qaTimeText.toString())

            val isValid = validate(qaDegreeText, qaTimeText, qaAttendanceType)
            Log.d("QA Validation", isValid.toString())

            val tableName =
                qaDegreeText + "_" + qaClassText + "_" + qaYearText + "_" + qaClassTypeText

            val attendanceStatus = utils.getAttendanceStatus(resources, qaAttendanceType.toString())

            Log.d("Modify Attendance Attendance Type", qaAttendanceType.toString())
            Log.d("MA Initial Attendance Status", attendanceStatus.first.toString())
            Log.d("MA Actual Attendance Status", attendanceStatus.second.toString())

            if (isValid) {
                val isInitiated = logic.attendanceLogic(
                    tableName,
                    attendanceStatus.first.toString(),
                    columnName,
                )

                val db = dbHelper.writableDatabase

                rollNoText = rollNo.editText!!.text.split(",").toTypedArray()
                lateralRollNoText = lateralRollNo.editText!!.text.split(",").toTypedArray()

                if (isInitiated == 1) {

                    qaLogic.quickAttendance(
                        db,
                        utils,
                        tableName,
                        rollNoText,
                        columnName,
                        lateralRollNoText,
                        attendanceStatus.second.toString()
                    )

                    val institutionId = utils.getInstitutionId(this)
                    val departmentName = utils.getDepartmentName(db, tableName)
                    val cloudClassName = "$qaDegreeText-$qaClassText-$qaYearText"

                    val cloudData =
                        FetchDataForCloud().fetchDataForCloudInsertion(db, tableName, columnName)

                    AttendanceDataToCloud().insertAttendanceData(
                        institutionId!!,
                        departmentName,
                        qaClassTypeText.toString(),
                        cloudClassName,
                        columnName,
                        cloudData
                    )

                    Toast.makeText(this, "Attendance Submitted Successfully", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                } else {
                    displayDialog(
                        db,
                        utils,
                        tableName,
                        Utils().ATTENDANCE_UPDATE_WARNING,
                        attendanceStatus.second.toString(),
                        columnName,
                        qaDegreeText, qaClassText, qaYearText, qaClassTypeText
                    )
                    Toast.makeText(this, "Make New Attendance After 45 Minutes", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun validate(degreeText: String?, timeText: String?, attendanceType: String?): Boolean {
        if (degreeText == null) {
            qaDegreeName.error = "Select a Degree"
            return false
        }
        if (timeText == null) {
            qaTimeName.error = "Select a Time"
            return false
        }
        if (attendanceType == null) {
            qaAttendanceTypeName.error = "Select a Attendance Type"
            return false
        }
        return true
    }

    private fun displayDialog(
        db: SQLiteDatabase,
        utils: Utils,
        tableName: String,
        message: String,
        actualAttendanceState: String,
        columnName: String,
        qaDegreeText: String?,
        qaClassText: String?,
        qaYearText: String?,
        qaClassTypeText: String?

    ) {

        val displayView: TextView = v.findViewById(R.id.alertbox)
        displayView.text = message
        builder.setView(v)
        builder.setTitle("WARNING").setPositiveButton("I, Understood") { _, _ ->

            qaLogic.quickAttendance(
                db,
                utils,
                tableName,
                rollNoText,
                columnName,
                lateralRollNoText,
                actualAttendanceState
            )

            val institutionId = utils.getInstitutionId(this)
            val departmentName = utils.getDepartmentName(db, tableName)
            val cloudClassName = "$qaDegreeText-$qaClassText-$qaYearText"

            val cloudData =
                FetchDataForCloud().fetchDataForCloudInsertion(db, tableName, columnName)

            AttendanceDataToCloud().insertAttendanceData(
                institutionId!!,
                departmentName,
                qaClassTypeText.toString(),
                cloudClassName,
                columnName,
                cloudData
            )

            Toast.makeText(this, "Attendance Updated", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        }.setNegativeButton("Cancel") { _, _ ->
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
}
