package com.keerthi77459.attendease.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.keerthi77459.attendease.R

class LoginScreen : AppCompatActivity() {

    private lateinit var departmentName: AutoCompleteTextView
    private lateinit var theoryClassDuration: AutoCompleteTextView
    private lateinit var labClassDuration: AutoCompleteTextView
    private lateinit var userNameText: TextView
    private lateinit var deptText: String
    private lateinit var theoryDurationText: String
    private lateinit var labDurationText: String
    private lateinit var button: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        userNameText = findViewById(R.id.text1)
        departmentName = findViewById(R.id.departmentName)
        theoryClassDuration = findViewById(R.id.theoryDuration)
        labClassDuration = findViewById(R.id.labDuration)
        button = findViewById(R.id.finishItOff)

        val sharedPreferences: SharedPreferences =
            this.getSharedPreferences("OnBoardingActivity", Context.MODE_PRIVATE)

        val userName = "Hello, ${sharedPreferences.getString("userName", "Professor")}"
        userNameText.text = userName

        val departmentsName =
            sharedPreferences.getStringSet("departmentsName", setOf())!!.toTypedArray()

        val departmentAdapter = ArrayAdapter(this, R.layout.drop_down_text, departmentsName)
        val theoryClassAdapter = ArrayAdapter(
            this,
            R.layout.drop_down_text,
            resources.getStringArray(R.array.theoryClassDuration)
        )
        val labClassAdapter = ArrayAdapter(
            this,
            R.layout.drop_down_text,
            resources.getStringArray(R.array.labClassDuration)
        )

        departmentName.setAdapter(departmentAdapter)
        theoryClassDuration.setAdapter(theoryClassAdapter)
        labClassDuration.setAdapter(labClassAdapter)

        departmentName.setOnItemClickListener { _, _, _, _ ->
            deptText = departmentName.text.toString()
        }

        theoryClassDuration.setOnItemClickListener { _, _, _, _ ->
            theoryDurationText = theoryClassDuration.text.toString()
        }

        labClassDuration.setOnItemClickListener { _, _, _, _ ->
            labDurationText = labClassDuration.text.toString()
        }

        button.setOnClickListener {
            val valid = validate(deptText, theoryDurationText, labDurationText)
            if (valid) {
//                  TODO : FUTURE ==> MAKE THE TIME SLOTS DYNAMIC
                val editor = sharedPreferences.edit()
                editor.putString("Department", deptText)
                editor.putString("TheoryDuration", theoryDurationText)
                editor.putString("LabDuration", labDurationText)
                editor.putBoolean("LoginStatus", true)
                editor.apply()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun validate(dept: String?, theoryDuration: String?, labDuration: String?): Boolean {
        if (dept == null) {
            departmentName.error = "Select a Department Name"
            return false
        }
        if (theoryDuration == null) {
            theoryClassDuration.error = "Select the Duration for Theory Class"
            return false
        }
        if (labDuration == null) {
            labClassDuration.error = "Select the Duration for Lab Class"
            return false
        }
        return true
    }
}