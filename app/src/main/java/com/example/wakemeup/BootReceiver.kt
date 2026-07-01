package com.example.wakemeup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
            val isAlarmActive = prefs.getBoolean("isAlarmActive", false)
            val scheduledAlarmTimeMillis = prefs.getLong("scheduledAlarmTimeMillis", 0)

            val shouldStartAlarm = if (isAlarmActive) {
                // The alarm was ringing when the phone was powered off
                true
            } else if (scheduledAlarmTimeMillis > 0 && scheduledAlarmTimeMillis <= System.currentTimeMillis()) {
                // The user missed the alarm while the phone was off
                true
            } else {
                false
            }

            if (shouldStartAlarm) {
                Log.d("WakeMeUp", "BootReceiver: Starting missed/interrupted alarm!")
                
                // Start the alarm service immediately
                val serviceIntent = Intent(context, AlarmService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                
                // Launch the activity to show the math problems
                val activityIntent = Intent(context, AlarmActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(activityIntent)
            }
        }
    }
}
