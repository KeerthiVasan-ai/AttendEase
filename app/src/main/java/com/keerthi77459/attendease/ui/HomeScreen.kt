package com.keerthi77459.attendease.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.keerthi77459.attendease.R
import com.keerthi77459.attendease.cloud.VersionCheck
import com.keerthi77459.attendease.utils.Utils

class HomeScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val sharedPreferences: SharedPreferences =
            this.getSharedPreferences("OnBoardingActivity", Context.MODE_PRIVATE)

        val loginStatus: Boolean = sharedPreferences.getBoolean("LoginStatus", false)
        val versionCheck = VersionCheck()

        Handler(Looper.getMainLooper()).postDelayed({

            versionCheck.checkVersion { result ->
                if (result != null) {
                    val (version, isUnderMaintenance) = result
                    println(result)
                    if (isUnderMaintenance) {
                        val intent = Intent(this, WarningScreen::class.java)
                        intent.putExtra("message", Utils().MAINTENANCE)
                        startActivity(intent)
                    } else if (version == "1.0.5" && loginStatus) {
                        startActivity(Intent(this, MainActivity::class.java))
                    } else if (version != "1.0.5" && loginStatus) {
                        val intent = Intent(this, WarningScreen::class.java)
                        intent.putExtra("message", Utils().VERSION_MISMATCH)
                        startActivity(intent)
                    } else {
                        startActivity(Intent(this, OnBoarding::class.java))
                    }
                } else {
                    println("Failed to fetch version details.")
                }
            }
        }, 3000)
    }
}