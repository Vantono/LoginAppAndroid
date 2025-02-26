package com.example.loginapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = "CREATE TABLE $TABLE_USERS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USERNAME TEXT, $COLUMN_EMAIL TEXT UNIQUE, $COLUMN_PASSWORD TEXT)"
        val createAttendanceTable = "CREATE TABLE $TABLE_ATTENDANCE ($COLUMN_ATTENDANCE_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USER_EMAIL TEXT, $COLUMN_DATE TEXT, $COLUMN_TIME TEXT, $COLUMN_ACTION TEXT)"
        db.execSQL(createUsersTable)
        db.execSQL(createAttendanceTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ATTENDANCE")
        onCreate(db)
    }

    fun clearAttendanceData() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_ATTENDANCE")
        Log.d("DatabaseHelper", "Attendance table cleared")
    }

    fun insertUser(username: String, email: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USERNAME, username)
        values.put(COLUMN_EMAIL, email)

        return try {
            db.insertOrThrow(TABLE_USERS, null, values) > 0
        } catch (e: Exception) {
            false
        }
    }

    fun updatePassword(email: String?, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put(COLUMN_PASSWORD, password) }
        return db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email)) > 0
    }

    fun validateUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?", arrayOf(email, password))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getAttendance(email: String): List<String> {
        val db = readableDatabase
        val attendanceList = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ATTENDANCE WHERE $COLUMN_USER_EMAIL = ? ORDER BY $COLUMN_ATTENDANCE_ID DESC", arrayOf(email))

        if (cursor.moveToFirst()) {
            do {
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
                val action = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTION))
                attendanceList.add("$date | $time | $action")
            } while (cursor.moveToNext())
        }
        cursor.close()
        return attendanceList
    }

    fun saveAttendance(email: String, action: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_DATE, SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            put(COLUMN_TIME, SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()))
            put(COLUMN_ACTION, action)
        }
        return db.insert(TABLE_ATTENDANCE, null, values) > 0

    }

    companion object {
        private const val DATABASE_NAME = "attendance.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val TABLE_ATTENDANCE = "attendance"
        private const val COLUMN_ATTENDANCE_ID = "attendance_id"
        private const val COLUMN_USER_EMAIL = "user_email"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_ACTION = "action"
    }
}
