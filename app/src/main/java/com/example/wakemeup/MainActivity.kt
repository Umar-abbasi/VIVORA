package com.example.wakemeup

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wakemeup.data.AlarmDatabase
import com.example.wakemeup.data.AlarmEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var alarmsRecyclerView: RecyclerView
    private lateinit var addAlarmFab: FloatingActionButton
    private lateinit var adapter: AlarmAdapter
    private lateinit var db: AlarmDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AlarmDatabase.getDatabase(this)
        alarmsRecyclerView = findViewById(R.id.alarmsRecyclerView)
        addAlarmFab = findViewById(R.id.addAlarmFab)

        adapter = AlarmAdapter { alarm, isChecked ->
            val updatedAlarm = alarm.copy(isActive = isChecked)
            lifecycleScope.launch {
                db.alarmDao().update(updatedAlarm)
                rescheduleAlarms()
            }
        }

        alarmsRecyclerView.adapter = adapter
        alarmsRecyclerView.layoutManager = LinearLayoutManager(this)

        addAlarmFab.setOnClickListener {
            if (checkPermissions()) {
                startActivity(Intent(this, AddAlarmActivity::class.java))
            }
        }

        // Observe alarms
        lifecycleScope.launch {
            db.alarmDao().getAllAlarms().collect { alarms ->
                adapter.setAlarms(alarms)
            }
        }
    }

    private fun checkPermissions(): Boolean {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "Please grant 'Draw over other apps' permission", Toast.LENGTH_LONG).show()
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                Toast.makeText(this, "Please grant 'Alarms & reminders' permission", Toast.LENGTH_LONG).show()
                return false
            }
        }
        return true
    }

    private fun rescheduleAlarms() {
        val intent = Intent(this, BootReceiver::class.java)
        intent.action = "android.intent.action.BOOT_COMPLETED"
        sendBroadcast(intent)
    }
}