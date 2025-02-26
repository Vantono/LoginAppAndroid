package com.example.loginapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AttendanceActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        dbHelper = DatabaseHelper(this)

        // Retrieve the email from the intent
        userEmail = intent.getStringExtra("user_email") ?: "vas@mail.com"

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Error: No user email provided!", Toast.LENGTH_SHORT).show()
            finish() // Close activity if email is missing
            return
        }

        val listView: ListView = findViewById(R.id.attendance_list)

        // Fetch attendance records for this user
        val attendanceRecords = dbHelper.getAttendance(userEmail)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, attendanceRecords)
        listView.adapter = adapter
    }
}