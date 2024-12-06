package com.keerthi77459.attendease.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.keerthi77459.attendease.R
import com.keerthi77459.attendease.cloud.DataFetch
import com.keerthi77459.attendease.cloud.GetDepartmentNamesFromCloud

class OnBoarding : AppCompatActivity() {

    private lateinit var userName: TextInputEditText
    private lateinit var institutionId: TextInputEditText
    private lateinit var button: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        val sharedPreferences: SharedPreferences =
            this.getSharedPreferences("OnBoardingActivity", Context.MODE_PRIVATE)

        userName = findViewById(R.id.userName)
        institutionId = findViewById(R.id.insitutionID)
        button = findViewById(R.id.letsGoButton)

        button.setOnClickListener {

            val userNameText = userName.text.toString()
            val institutionIdText = institutionId.text.toString()
            val valid = validate(userNameText, institutionIdText)

            if (valid) {
                GetDepartmentNamesFromCloud().getDepartmentNames(
                    institutionIdText,
                    object : DataFetch {
                        override fun onSuccess(data: Array<String>) {
                            val editor: SharedPreferences.Editor = sharedPreferences.edit()
                            editor.putString("userName", userNameText)
                            editor.putString("institutionId", institutionIdText)
                            editor.putStringSet("departmentsName", data.toSet())
                            editor.apply()

                            val intent = Intent(this@OnBoarding, LoginScreen::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }

                        override fun onFailure(errorMessage: String) {
                            Toast.makeText(this@OnBoarding, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    })
            }
        }


    }

    private fun validate(userNameText: String, institutionIdText: String): Boolean {
        if (userNameText.trim().isEmpty()) {
            userName.error = "Enter your Name"
            return false
        }
        if (institutionIdText.trim().isEmpty()) {
            institutionId.error = "Enter your Institution Id"
            return false
        }
        return true
    }
}
