package com.keerthi77459.attendease

import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout

class QuickAttendance : AppCompatActivity() {

    lateinit var submitAttendance: Button
    private lateinit var qaClassName: TextInputLayout
    lateinit var rollNo: TextInputLayout
    lateinit var lateralRollNo: TextInputLayout
    lateinit var qaDegreeName: AutoCompleteTextView
    lateinit var qaYearName:AutoCompleteTextView
    lateinit var qaSemester: Array<String>
    lateinit var qaDegree: Array<String>
    lateinit var rollNoText: Array<String>
    lateinit var lateralRollNoText: Array<String>
    private lateinit var qaDegreeText: String
    private lateinit var qaYearText: String
    lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_attendance)

        dbHelper = DbHelper(this)

        val resource = resources
        qaDegree = resource.getStringArray(R.array.degree)
        qaSemester = resource.getStringArray(R.array.semester)

        qaDegreeName = findViewById(R.id.qaDegreeName)
        qaClassName = findViewById(R.id.qaClassNameField)
        qaYearName = findViewById(R.id.qaYearName)
        submitAttendance = findViewById(R.id.submitAttendance)
        rollNo = findViewById(R.id.rollNoField)
        lateralRollNo = findViewById(R.id.lateralRollNoField)

        val degreeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, R.layout.drop_down_text, qaDegree)
        val semesterAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, R.layout.drop_down_text, qaSemester)

        qaYearName.setAdapter(semesterAdapter)
        qaDegreeName.setAdapter(degreeAdapter)

        qaYearName.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            qaYearText =qaYearName.text.toString()
        }

        qaDegreeName.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            qaDegreeText = qaDegreeName.text.toString()
        }

        submitAttendance.setOnClickListener {
            rollNoText = rollNo.editText!!.text.split(",").toTypedArray()
            println(rollNoText[0])
        }



    }
}
