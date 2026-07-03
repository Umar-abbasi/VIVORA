package com.example.wakemeup

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wakemeup.data.AlarmDatabase
import com.example.wakemeup.data.AlarmEntity
import kotlinx.coroutines.launch

class AddAlarmActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var difficultySpinner: Spinner
    private lateinit var numProblemsEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_alarm)

        timePicker = findViewById(R.id.timePicker)
        difficultySpinner = findViewById(R.id.difficultySpinner)
        numProblemsEditText = findViewById(R.id.numProblemsEditText)
        saveButton = findViewById(R.id.saveButton)

        timePicker.setIs24HourView(true)

        val difficulties = arrayOf("Easy", "Medium", "Hard")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficulties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        difficultySpinner.adapter = adapter

        saveButton.setOnClickListener {
            saveAlarm()
        }
    }

    private fun saveAlarm() {
        val hour = timePicker.hour
        val minute = timePicker.minute
        val difficulty = difficultySpinner.selectedItem.toString()
        val numProblemsStr = numProblemsEditText.text.toString()
        val numProblems = numProblemsStr.toIntOrNull()

        if (numProblems == null || numProblems <= 0) {
            Toast.makeText(this, "Please enter a valid number of problems", Toast.LENGTH_SHORT).show()
            return
        }

        // For phase 1 simplicity, we just set daysOfWeek to "Everyday" or leave empty string to handle dynamically later
        val newAlarm = AlarmEntity(
            hour = hour,
            minute = minute,
            daysOfWeek = "1,2,3,4,5,6,7", // All days
            isActive = true,
            difficultyLevel = difficulty,
            numProblems = numProblems
        )

        val db = AlarmDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.alarmDao().insert(newAlarm)
            Toast.makeText(this@AddAlarmActivity, "Alarm Saved", Toast.LENGTH_SHORT).show()
            
            // Broadcast intent to BootReceiver or AlarmManager to reschedule alarms
            val intent = Intent(this@AddAlarmActivity, BootReceiver::class.java)
            intent.action = "android.intent.action.BOOT_COMPLETED" // trigger reschedule
            sendBroadcast(intent)
            
            finish()
        }
    }
}
