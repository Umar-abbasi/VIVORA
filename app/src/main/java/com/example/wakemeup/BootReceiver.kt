package com.example.wakemeup

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.wakemeup.data.AlarmDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_LOCKED_BOOT_COMPLETED || action == "android.intent.action.BOOT_COMPLETED") {
            
            val db = AlarmDatabase.getDatabase(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            CoroutineScope(Dispatchers.IO).launch {
                val activeAlarms = db.alarmDao().getActiveAlarms()
                
                for (alarm in activeAlarms) {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
                    calendar.set(Calendar.MINUTE, alarm.minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    
                    if (calendar.timeInMillis <= System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    
                    // Note: For phase 1 we assume it triggers every day. 
                    // To handle specific days of week, we would calculate the exact next day.
                    
                    val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                        putExtra("ALARM_ID", alarm.id)
                    }
                    
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        alarm.id, // Use alarm ID as request code to distinguish multiple alarms
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    
                    try {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                        Log.d("Vivora", "Scheduled alarm ${alarm.id} for ${calendar.time}")
                    } catch (e: SecurityException) {
                        Log.e("Vivora", "Permission to schedule exact alarm denied", e)
                    }
                }
            }
        }
    }
}
