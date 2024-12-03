package com.keerthi77459.attendease.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.keerthi77459.attendease.R
import com.keerthi77459.attendease.db.DbHelper
import com.keerthi77459.attendease.model.ClassData
import com.keerthi77459.attendease.utils.Utils
import com.keerthi77459.attendease.viewmodel.Logic
import com.keerthi77459.attendease.viewmodel.MapDateAndTime
import com.keerthi77459.attendease.viewmodel.QuickAttendanceLogic

class ModifyAttendance : AppCompatActivity() {

    private lateinit var modifyAttendance: Button
    private lateinit var date: TextInputEditText
    private lateinit var time: TextInputEditText

    private lateinit var overallClassDetails: ArrayList<String>

    private var maDegreeText: String? = null
    private var maClassText: String? = null
    private var maYearText: String? = null
    private var maAttendanceType: String? = null

    private lateinit var qaLogic: QuickAttendanceLogic
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var maDegreeName: AutoCompleteTextView
    private lateinit var maAttendanceTypeName: AutoCompleteTextView

    private lateinit var rollNo: TextInputLayout
    private lateinit var lateralRollNo: TextInputLayout

    private lateinit var rollNoText: Array<String>
    private lateinit var lateralRollNoText: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_attendance)

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
            maYearText = maDegreeName.text.toString().split("-")[2]
            maClassText = maDegreeName.text.toString().split("-")[1]
        }

        maAttendanceTypeName.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            maAttendanceType = maAttendanceTypeName.text.toString()
        }

        val materialDateBuilder: MaterialDatePicker.Builder<*> =
            MaterialDatePicker.Builder.datePicker().setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        date.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        materialDatePicker.addOnPositiveButtonClickListener {
            date.setText(materialDatePicker.headerText)
        }


        val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
            .setTitleText("SELECT YOUR TIMING")
            .setHour(12)
            .setMinute(10)
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .build()

        time.setOnClickListener {
            materialTimePicker.show(supportFragmentManager, "MATERIAL_TIME_PICKER")
        }

        materialTimePicker.addOnPositiveButtonClickListener {
            val pickedHour: Int = materialTimePicker.hour
            val pickedMinute: Int = materialTimePicker.minute

            val formattedTime: String = when {
                pickedHour > 12 -> {
                    if (pickedMinute < 10) {
                        "${materialTimePicker.hour - 12}:0${materialTimePicker.minute} pm"
                    } else {
                        "${materialTimePicker.hour - 12}:${materialTimePicker.minute} pm"
                    }
                }

                pickedHour == 12 -> {
                    if (pickedMinute < 10) {
                        "${materialTimePicker.hour}:0${materialTimePicker.minute} pm"
                    } else {
                        "${materialTimePicker.hour}:${materialTimePicker.minute} pm"
                    }
                }

                pickedHour == 0 -> {
                    if (pickedMinute < 10) {
                        "${materialTimePicker.hour + 12}:0${materialTimePicker.minute} am"
                    } else {
                        "${materialTimePicker.hour + 12}:${materialTimePicker.minute} am"
                    }
                }

                else -> {
                    if (pickedMinute < 10) {
                        "${materialTimePicker.hour}:0${materialTimePicker.minute} am"
                    } else {
                        "${materialTimePicker.hour}:${materialTimePicker.minute} am"
                    }
                }
            }
            time.setText(formattedTime)
        }

        modifyAttendance.setOnClickListener {

            maDegreeName.error = null
            date.error = null
            time.error = null
            maAttendanceTypeName.error = null

            val dateValue = date.text.toString()
            val timeValue = time.text.toString().split(" ")[0]

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

                val date = utils.returnDate(dateValue.split(" ")[0])

                val columnName =
                    "_${date}_${MapDateAndTime().timeDate[dateValue.split(" ")[1]]}_${
                        dateValue.split(" ")[2]
                    }_${timeValue.split(":")[0]}_${timeValue.split(":")[1]}_00"

                Log.d("Modify Attendance Column Name", columnName)

                val isInitiated = logic.attendanceLogic(
                    sharedPreferences,
                    -1,
                    "",
                    tableName,
                    attendanceStatus.first.toString(),
                    columnName,
                    false
                )
                println(isInitiated)

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
                }
            }

            Toast.makeText(this, "Attendance Submitted Successfully", Toast.LENGTH_LONG)
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
}