package com.example.wakemeup.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: String, // Comma-separated list of Calendar days (e.g. "2,3,4,5,6" for Mon-Fri)
    val isActive: Boolean,
    val difficultyLevel: String, // "Easy", "Medium", "Hard"
    val numProblems: Int
)
