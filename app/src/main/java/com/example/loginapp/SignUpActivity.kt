package com.example.loginapp

import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.loginapp.R.layout.activity_signup

class SignUpActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SignUpActivity", "SignUpActivity Started!") // Ελέγχει αν η δραστηριότητα ξεκινάει σωστά
        setContentView(activity_signup)



        val usernameEditText = findViewById<EditText>(R.id.username)
        val emailEditText = findViewById<EditText>(R.id.email)
        val signUpButton = findViewById<Button>(R.id.button)

        dbHelper = DatabaseHelper(this)

        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            if (validateInput(username, email)) {
                if (dbHelper.insertUser(username, email)) {
                    Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SetPasswordActivity::class.java)
                    intent.putExtra("email", email)  // Pass email to next screen
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "User already exists!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateInput(username: String, email: String): Boolean {
        return when {
            username.isEmpty() -> {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                false
            }
            email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
}
