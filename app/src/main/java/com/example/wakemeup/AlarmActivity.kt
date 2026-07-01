package com.example.wakemeup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.KeyEvent
import kotlin.random.Random

class AlarmActivity : AppCompatActivity() {
    private lateinit var progressTextView: TextView
    private lateinit var mathProblemTextView: TextView
    private lateinit var answerEditText: EditText
    private lateinit var submitAnswerButton: Button
    
    private var solvedCount = 0
    private val requiredSolves = 10
    private var currentAnswer = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        
        setContentView(R.layout.activity_alarm)
        
        progressTextView = findViewById(R.id.progressTextView)
        mathProblemTextView = findViewById(R.id.mathProblemTextView)
        answerEditText = findViewById(R.id.answerEditText)
        submitAnswerButton = findViewById(R.id.submitAnswerButton)
        
        generateNewProblem()
        updateProgress()
        
        submitAnswerButton.setOnClickListener {
            checkAnswer()
        }
        
        // Pin the screen so the user cannot use Home or Recents buttons
        startLockTask()
    }
    
    private fun generateNewProblem() {
        val num1 = Random.nextInt(10, 100)
        val num2 = Random.nextInt(10, 100)
        currentAnswer = num1 + num2
        mathProblemTextView.text = "$num1 + $num2 = ?"
        answerEditText.text.clear()
    }
    
    private fun updateProgress() {
        progressTextView.text = "Solved: $solvedCount / $requiredSolves"
    }
    
    private fun checkAnswer() {
        val inputStr = answerEditText.text.toString()
        if (inputStr.isEmpty()) return
        
        val inputInt = inputStr.toIntOrNull()
        if (inputInt == currentAnswer) {
            solvedCount++
            if (solvedCount >= requiredSolves) {
                stopAlarmAndFinish()
            } else {
                generateNewProblem()
                updateProgress()
            }
        } else {
            Toast.makeText(this, "Incorrect! Try again.", Toast.LENGTH_SHORT).show()
            answerEditText.text.clear()
        }
    }
    
    private fun stopAlarmAndFinish() {
        val prefs = getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("isAlarmActive", false)
            .putLong("scheduledAlarmTimeMillis", 0)
            .apply()

        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP_ALARM"
        }
        startService(stopIntent)
        
        // Unpin the screen before finishing
        stopLockTask()
        finish()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Toast.makeText(this, "You must solve the problems first!", Toast.LENGTH_SHORT).show()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Intercept volume buttons so they do absolutely nothing
        val action = event.action
        val keyCode = event.keyCode
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_MUTE -> {
                true // Consume the event
            }
            else -> super.dispatchKeyEvent(event)
        }
    }
}
