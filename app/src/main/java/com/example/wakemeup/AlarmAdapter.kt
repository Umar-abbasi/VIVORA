package com.example.wakemeup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wakemeup.data.AlarmEntity
import com.google.android.material.materialswitch.MaterialSwitch

class AlarmAdapter(
    private val onAlarmToggle: (AlarmEntity, Boolean) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    private var alarms = emptyList<AlarmEntity>()

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val daysTextView: TextView = itemView.findViewById(R.id.daysTextView)
        val difficultyTextView: TextView = itemView.findViewById(R.id.difficultyTextView)
        val alarmSwitch: MaterialSwitch = itemView.findViewById(R.id.alarmSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val current = alarms[position]
        
        val hourStr = String.format("%02d", if (current.hour % 12 == 0) 12 else current.hour % 12)
        val minuteStr = String.format("%02d", current.minute)
        val amPm = if (current.hour >= 12) "PM" else "AM"
        
        holder.timeTextView.text = "$hourStr:$minuteStr $amPm"
        holder.daysTextView.text = if (current.daysOfWeek == "1,2,3,4,5,6,7") "Everyday" else current.daysOfWeek
        holder.difficultyTextView.text = "${current.difficultyLevel} | ${current.numProblems} Problems"
        
        // Remove listener temporarily to avoid triggering on state restoration
        holder.alarmSwitch.setOnCheckedChangeListener(null)
        holder.alarmSwitch.isChecked = current.isActive
        holder.alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            onAlarmToggle(current, isChecked)
        }
    }

    override fun getItemCount() = alarms.size

    fun setAlarms(alarms: List<AlarmEntity>) {
        this.alarms = alarms
        notifyDataSetChanged()
    }
}
