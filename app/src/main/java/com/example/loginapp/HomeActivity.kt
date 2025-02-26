package com.example.loginapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.concurrent.Executor

class HomeActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dbHelper: DatabaseHelper
    private val officeLatitude = 37.9838 // Example latitude
    private val officeLongitude = 23.7275 // Example longitude
    private val userEmail = "user@example.com" // TODO: Replace with actual logged-in user email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        dbHelper = DatabaseHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val checkInButton = findViewById<Button>(R.id.checkin_button)
        val checkOutButton = findViewById<Button>(R.id.checkout_button)
        val viewAttendanceButton = findViewById<Button>(R.id.view_attendance_button)

        checkInButton.setOnClickListener {
            authenticateUser { verifyLocation { recordAttendance("Check-in") } }
        }

        checkOutButton.setOnClickListener {
            authenticateUser { verifyLocation { recordAttendance("Check-out") } }
        }

        viewAttendanceButton.setOnClickListener {
            val intent = Intent(this, AttendanceActivity::class.java)
            intent.putExtra("user_email", userEmail)
            startActivity(intent)
        }
    }

    private fun authenticateUser(onSuccess: () -> Unit) {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor: Executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(this, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onSuccess()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Toast.makeText(applicationContext, "Biometric authentication failed", Toast.LENGTH_SHORT).show()
                        }
                    })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric Authentication")
                    .setSubtitle("Authenticate to proceed")
                    .setNegativeButtonText("Cancel")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Toast.makeText(this, "No biometric hardware detected", Toast.LENGTH_SHORT).show()

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Toast.makeText(this, "Biometric hardware is unavailable", Toast.LENGTH_SHORT).show()

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Toast.makeText(this, "No biometric credentials enrolled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyLocation(onSuccess: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val distance = FloatArray(1)
                Location.distanceBetween(it.latitude, it.longitude, officeLatitude, officeLongitude, distance)
                if (distance[0] < 10000000000000) {
                    onSuccess()
                } else {
                    Toast.makeText(this, "You must be at the office to check-in/out", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun recordAttendance(type: String) {
        if (dbHelper.saveAttendance(userEmail, type)) {
            Toast.makeText(this, "$type recorded successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to record $type", Toast.LENGTH_SHORT).show()
        }
    }
}
