package com.keerthi77459.attendease.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.keerthi77459.attendease.R
import com.keerthi77459.attendease.db.DbHelper
import com.keerthi77459.attendease.model.AttendanceData
import com.keerthi77459.attendease.model.ClassData
import com.keerthi77459.attendease.utils.Utils
import com.keerthi77459.attendease.viewmodel.FetchAttendanceData
import com.keerthi77459.attendease.viewmodel.FetchAttendanceDataNew
import com.keerthi77459.attendease.viewmodel.MapDateAndTime

class ViewAttendance : AppCompatActivity() {

    private lateinit var viewAttendance: Button
    private lateinit var date: TextInputEditText
    private lateinit var vaDegreeName: AutoCompleteTextView
    private lateinit var vaAttendanceTypeName: AutoCompleteTextView

    private lateinit var overallClassDetails: ArrayList<String>

    private var vaDegreeText: String? = null
    private var vaClassText: String? = null
    private var vaYearText: String? = null
    private var vaClassTypeText: String? = null
    private var vaAttendanceType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_attendance)

        date = findViewById(R.id.vaDate)
        viewAttendance = findViewById(R.id.viewAttendance)
        vaDegreeName = findViewById(R.id.vaDegreeName)
        vaAttendanceTypeName = findViewById(R.id.vaAttendanceTypeName)

        val utils = Utils()
        val dbHelper = DbHelper(this)
        val fetchAttendanceData = FetchAttendanceData()
        val classData = ClassData(this)
        overallClassDetails = classData.mergedClassDetails()

        val degreeAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.drop_down_text, overallClassDetails)

        val attendanceTypeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this, R.layout.drop_down_text, resources.getStringArray(R.array.attendance_type)
        )

        vaDegreeName.setAdapter(degreeAdapter)
        vaAttendanceTypeName.setAdapter(attendanceTypeAdapter)

        vaDegreeName.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            vaDegreeText = vaDegreeName.text.toString().split("-")[0]
            vaClassText = vaDegreeName.text.toString().split("-")[1]
            vaYearText = vaDegreeName.text.toString().split("-")[2]
            vaClassTypeText = vaDegreeName.text.toString().split("-")[3]
        }

        vaAttendanceTypeName.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            vaAttendanceType = vaAttendanceTypeName.text.toString()
        }

        val materialDateBuilder: MaterialDatePicker.Builder<Long> =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("SELECT A DATE")
                .setCalendarConstraints(
                    CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointBackward.now())
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

        viewAttendance.setOnClickListener {

            vaDegreeName.error = null
            date.error = null
            vaAttendanceTypeName.error = null

            val dateValue = date.text.toString()

            val isValid = validate(
                vaDegreeText, date.text.toString(), vaAttendanceType
            )
            Log.d("VA Validation", isValid.toString())

            if (isValid) {
                val db = dbHelper.writableDatabase

                val date = dateValue.split(" ")[0]
                val month = MapDateAndTime().timeDate[dateValue.split(" ")[1]]
                val year = dateValue.split(" ")[2]

                val tableName =
                    vaDegreeText + "_" + vaClassText + "_" + vaYearText + "_" + vaClassTypeText

                val attendanceStatus = when (vaAttendanceType) {
                    resources.getStringArray(R.array.attendance_type)[0] -> utils.ATTENDANCE_ABSENT
                    resources.getStringArray(R.array.attendance_type)[1] -> utils.ATTENDANCE_PRESENT
                    else -> ""
                }
                println(dateValue)
                println(tableName)

                val viewAttendanceData =
                    FetchAttendanceDataNew().fetchTrueValuesGroupedByColumnAndMode(
                        db, tableName, date, month.toString(), year, attendanceStatus
                    )


                println(viewAttendanceData)

                val resultData =
                    ArrayList(viewAttendanceData.map { AttendanceData(it.key, it.value) })
                val intent = Intent(this, ViewAttendanceList::class.java)
                intent.putExtra("tableName", tableName)
                intent.putExtra("attendanceType", vaAttendanceType)
                intent.putExtra("resultData", resultData)
                startActivity(intent)
            }
        }
    }

    private fun validate(degree: String?, dateValue: String?, attendanceType: String?): Boolean {
        if (degree == null) {
            vaDegreeName.error = "Select a Degree"
            return false
        }
        if (dateValue?.trim()?.isEmpty()!!) {
            date.error = "Select a Date"
            return false
        }
        if (attendanceType == null) {
            vaAttendanceTypeName.error = "Select a Attendance Type"
            return false
        }
        return true
    }
}