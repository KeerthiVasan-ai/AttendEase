package com.keerthi77459.attendease.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.keerthi77459.attendease.R

class HomeScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val sharedPreferences: SharedPreferences =
            this.getSharedPreferences("OnBoardingActivity", Context.MODE_PRIVATE)

        val loginStatus: Boolean = sharedPreferences.getBoolean("LoginStatus", false)

        Handler(Looper.getMainLooper()).postDelayed({
            if (loginStatus) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, OnBoarding::class.java))
            }
        }, 3000)
    }
}