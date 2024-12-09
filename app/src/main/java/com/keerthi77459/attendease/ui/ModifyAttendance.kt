package com.keerthi77459.attendease.ui

import android.annotation.SuppressLint
import android.content.Context
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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.keerthi77459.attendease.R
import com.keerthi77459.attendease.db.DbHelper
import com.keerthi77459.attendease.model.ClassData
import com.keerthi77459.attendease.utils.Utils
import com.keerthi77459.attendease.viewmodel.Logic
import com.keerthi77459.attendease.viewmodel.QuickAttendanceLogic

class ModifyAttendance : AppCompatActivity() {

    private lateinit var modifyAttendance: Button
    private lateinit var date: TextInputEditText
    private lateinit var time: AutoCompleteTextView

    private lateinit var overallClassDetails: ArrayList<String>

    private var maDegreeText: String? = null
    private var maClassText: String? = null
    private var maYearText: String? = null
    private var maClassType: String? = null
    private var maAttendanceType: String? = null

    private lateinit var qaLogic: QuickAttendanceLogic
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var maDegreeName: AutoCompleteTextView
    private lateinit var maAttendanceTypeName: AutoCompleteTextView

    private lateinit var rollNo: TextInputLayout
    private lateinit var lateralRollNo: TextInputLayout

    private lateinit var rollNoText: Array<String>
    private lateinit var lateralRollNoText: Array<String>

    private lateinit var builder: AlertDialog.Builder
    private lateinit var v: View
    private lateinit var dialog: AlertDialog

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_attendance)

        builder = AlertDialog.Builder(this)
        v = LayoutInflater.from(this).inflate(R.layout.fragement_alertbox, null)

        date = findViewById(R.id.dateField)
        time = findViewById(R.id.timeField)
        maDegreeName = findViewById(R.id.maDegreeName)
        maAttendanceTypeName = findViewById(R.id.maAttendanceTypeName)
        modifyAttendance = findViewById(R.id.modifyAttendance)
        rollNo = findViewById(R.id.rollNoField)
        lateralRollNo = findViewById(R.id.lateralRollNoField)

        val utils = Utils()
        val dbHelper = DbHelper(this)
        val classData = ClassData(this)
        val logic = Logic(this)
        qaLogic = QuickAttendanceLogic(this)
        overallClassDetails = classData.mergedClassDetails()

        val degreeAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.drop_down_text, overallClassDetails)

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

        val attendanceTypeAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                this,
                R.layout.drop_down_text,
                resources.getStringArray(R.array.attendance_type)
            )

        maDegreeName.setAdapter(degreeAdapter)
        maAttendanceTypeName.setAdapter(attendanceTypeAdapter)

        maDegreeName.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            maDegreeText = maDegreeName.text.toString().split("-")[0]
            maClassText = maDegreeName.text.toString().split("-")[1]
            maYearText = maDegreeName.text.toString().split("-")[2]
            maClassType = maDegreeName.text.toString().split("-")[3]

            if (maClassType == resources.getStringArray(R.array.class_type)[0]) {
                time.setAdapter(theorySlotAdapter)
            } else if (maClassType == resources.getStringArray(R.array.class_type)[1]) {
                time.setAdapter(labSlotAdapter)
            }
        }

        maAttendanceTypeName.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            maAttendanceType = maAttendanceTypeName.text.toString()
        }

        val materialDateBuilder: MaterialDatePicker.Builder<Long> =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("SELECT A DATE")
                .setCalendarConstraints(
                    CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointBackward.now()) // Restrict to past and current dates
                        .build()
                )
        val materialDatePicker = materialDateBuilder.build()

        date.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            val formattedDate = sdf.format(selection)
            date.setText(formattedDate)
        }

        modifyAttendance.setOnClickListener {

            maDegreeName.error = null
            date.error = null
            time.error = null
            maAttendanceTypeName.error = null

            val dateValue = date.text.toString()
            val timeValue = time.text.toString()

            val isValid = validate(maDegreeText, dateValue, timeValue, maAttendanceType)
            Log.d("MA Validation", isValid.toString())

            val tableName = maDegreeText + "_" + maClassText + "_" + maYearText

            sharedPreferences = this.getSharedPreferences("DoOnce", Context.MODE_PRIVATE)

            val attendanceStatus =
                utils.getAttendanceStatus(resources, maAttendanceType.toString())

            Log.d("Modify Attendance Attendance Type", maAttendanceType.toString())
            Log.d("MA Initial Attendance Status", attendanceStatus.first.toString())
            Log.d("MA Actual Attendance Status", attendanceStatus.second.toString())

            if (isValid) {

                val columnName = utils.getColumnName(dateValue, timeValue)
                Log.d("Modify Attendance Column Name", columnName)

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
                } else {
                    displayDialog(
                        db,
                        utils,
                        tableName,
                        utils.ATTENDANCE_UPDATE_WARNING,
                        attendanceStatus.second.toString(),
                        columnName
                    )
                }
            }

            Toast.makeText(this, "Attendance Submitted Successfully", Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun validate(
        degreeText: String?,
        dateValue: String?,
        timeValue: String?,
        attendanceType: String?
    ): Boolean {
        if (degreeText == null) {
            maDegreeName.error = "Select a Degree"
            return false
        }
        if (dateValue?.trim()?.isEmpty()!!) {
            date.error = "Select the Date"
            return false
        }
        if (timeValue?.trim()?.isEmpty()!!) {
            time.error = "Select the Time"
            return false
        }
        if (attendanceType == null) {
            maAttendanceTypeName.error = "Select Attendance Type"
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
        columnName: String
    ) {

        val displayView: TextView = v.findViewById(R.id.alertbox)
        displayView.text = message
        builder.setView(v)
        builder.setTitle("WARNING")
            .setPositiveButton("I, Understood") { _, _ ->

                qaLogic.quickAttendance(
                    db,
                    utils,
                    tableName,
                    rollNoText,
                    columnName,
                    lateralRollNoText,
                    actualAttendanceState
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