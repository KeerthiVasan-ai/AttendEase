package com.keerthi77459.attendease.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.keerthi77459.attendease.R

class WarningScreen : AppCompatActivity() {


    private lateinit var userNameText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning_screen)

        userNameText = findViewById(R.id.text1)

        val sharedPreferences: SharedPreferences =
            this.getSharedPreferences("OnBoardingActivity", Context.MODE_PRIVATE)

        val message = intent.getStringExtra("message")

        val userName = "Hello, ${sharedPreferences.getString("userName", "Professor")}"
        val completeMessage = userName + "\n" + message
        userNameText.text = completeMessage
    }
}