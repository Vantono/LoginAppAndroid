package com.example.loginapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

 class SetPasswordActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_password)

        dbHelper = DatabaseHelper(this)
        userEmail = intent.getStringExtra("email")

        val passwordEditText = findViewById<EditText>(R.id.passSet)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirm_password)
        val savePasswordButton = findViewById<Button>(R.id.save_password_button)

        savePasswordButton.setOnClickListener {
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (validatePasswords(password, confirmPassword)) {
                if (dbHelper.updatePassword(userEmail, password)) {
                    Toast.makeText(this, "Password set successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error setting password!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validatePasswords(password: String, confirmPassword: String): Boolean {
        return when {
            password.isEmpty() || confirmPassword.isEmpty() -> {
                Toast.makeText(this, "Password fields cannot be empty", Toast.LENGTH_SHORT).show()
                false
            }
            password != confirmPassword -> {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                false
            }
            password.length < 6 -> {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
}
