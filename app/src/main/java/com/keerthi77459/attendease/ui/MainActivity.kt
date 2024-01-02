package com.keerthi77459.attendease.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.keerthi77459.attendease.R
import com.keerthi77459.attendease.adapter.ClassAdapter
import com.keerthi77459.attendease.model.ClassData

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recycler1: RecyclerView
    private lateinit var classAdapter: ClassAdapter
//    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val classData = ClassData(this)
//        val logic = Logic(this)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)

        val isFetched = classData.getClass()
        if (isFetched == 0) {
            Toast.makeText(this, "No Class Created", Toast.LENGTH_LONG).show()
        }

//        APP BAR
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "AttendEase"

//        NAVIGATION DRAWER
        navigationView.setNavigationItemSelectedListener(this)
        val toggle =
            ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        recycler1 = findViewById(R.id.recycle1)
        classAdapter = ClassAdapter(
            this,
            classData.degreeName,
            classData.className,
            classData.yearName
        )
        recycler1.adapter = classAdapter
        recycler1.layoutManager = LinearLayoutManager(this)

//        sharedPreferences = this.getSharedPreferences("DoOnce", Context.MODE_PRIVATE)
//        val lastDay: Int = sharedPreferences.getInt("LastRunClassDay", -1)
//        logic.initialLogic(sharedPreferences,lastDay)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addClass -> {
                startActivity(Intent(this, AddClass::class.java))
            }
            R.id.manageClass -> {
                startActivity(Intent(this, ManageClass::class.java))
            }
            R.id.quickAttendance -> {
                startActivity(Intent(this, QuickAttendance::class.java))
            }
            R.id.generateReport -> {
                startActivity(Intent(this, GenerateReport::class.java))
            }
        }
        return true
    }
}