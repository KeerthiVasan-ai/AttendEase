package com.keerthi77459.attendease.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.keerthi77459.attendease.R

class WarningScreen : AppCompatActivity() {

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

        val sharedPreferences: SharedPreferences =
            this.getSharedPreferences("OnBoardingActivity", Context.MODE_PRIVATE)

        val message = intent.getStringExtra("message")

        val userName = "Hello, ${sharedPreferences.getString("userName", "Professor")}"
        val completeMessage = userName + "\n" + message
        userNameText.text = completeMessage
    }
}