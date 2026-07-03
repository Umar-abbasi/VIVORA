package com.example.wakemeup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wakemeup.data.AlarmDatabase
import kotlinx.coroutines.launch
import kotlin.random.Random

class AlarmActivity : AppCompatActivity() {
    private lateinit var progressTextView: TextView
    private lateinit var mathProblemTextView: TextView
    private lateinit var answerEditText: EditText
    private lateinit var submitAnswerButton: Button
    
    private var solvedCount = 0
    private var requiredSolves = 10
    private var currentAnswer = 0
    private var difficultyLevel = "Medium"
    private var alarmId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        
        setContentView(R.layout.activity_alarm)
        
        progressTextView = findViewById(R.id.progressTextView)
        mathProblemTextView = findViewById(R.id.mathProblemTextView)
        answerEditText = findViewById(R.id.answerEditText)
        submitAnswerButton = findViewById(R.id.submitAnswerButton)
        
        alarmId = intent.getIntExtra("ALARM_ID", -1)
        if (alarmId != -1) {
            val db = AlarmDatabase.getDatabase(this)
            lifecycleScope.launch {
                val alarm = db.alarmDao().getAlarmById(alarmId)
                if (alarm != null) {
                    requiredSolves = alarm.numProblems
                    difficultyLevel = alarm.difficultyLevel
                }
                
                generateNewProblem()
                updateProgress()
            }
        } else {
            generateNewProblem()
            updateProgress()
        }
        
        submitAnswerButton.setOnClickListener {
            checkAnswer()
        }
        
        // Pin the screen so the user cannot use Home or Recents buttons
        startLockTask()
    }
    
    private fun generateNewProblem() {
        val (min, max) = when (difficultyLevel) {
            "Easy" -> Pair(1, 10)
            "Hard" -> Pair(100, 1000)
            else -> Pair(10, 100) // Medium
        }
        
        val num1 = Random.nextInt(min, max)
        val num2 = Random.nextInt(min, max)
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
        if (alarmId != -1) {
            val db = AlarmDatabase.getDatabase(this)
            lifecycleScope.launch {
                val alarm = db.alarmDao().getAlarmById(alarmId)
                if (alarm != null) {
                    // Disable the alarm for next day unless it's a repeating alarm
                    // Since Phase 1 handles simple scheduling, we can turn it off
                    db.alarmDao().update(alarm.copy(isActive = false))
                }
            }
        }

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
