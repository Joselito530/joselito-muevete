package com.example.joselitomueveteomuere

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import java.util.Calendar

object AlarmScheduler {

    private const val PREFS_NAME = "joselito_prefs"
    private const val KEY_INTERVAL_MINUTES = "interval_minutes"
    private const val KEY_START_HOUR = "start_hour"
    private const val KEY_START_MINUTE = "start_minute"
    private const val KEY_END_HOUR = "end_hour"
    private const val KEY_END_MINUTE = "end_minute"
    private const val KEY_SOUND = "sound_enabled"
    private const val KEY_VIBRATION = "vibration_enabled"
    private const val KEY_OVERLAY = "overlay_enabled"
    private const val KEY_ACTIVE = "alarm_active"

    const val REQUEST_CODE = 1001

    fun scheduleNext(context: Context) {
        val prefs = getPrefs(context)
        if (!prefs.getBoolean(KEY_ACTIVE, false)) return

        val intervalMinutes = prefs.getInt(KEY_INTERVAL_MINUTES, 60)
        val startHour = prefs.getInt(KEY_START_HOUR, 8)
        val startMinute = prefs.getInt(KEY_START_MINUTE, 0)
        val endHour = prefs.getInt(KEY_END_HOUR, 22)
        val endMinute = prefs.getInt(KEY_END_MINUTE, 0)

        val next = Calendar.getInstance().apply {
            add(Calendar.MINUTE, intervalMinutes)
        }

        val nextHour = next.get(Calendar.HOUR_OF_DAY)
        val nextMin = next.get(Calendar.MINUTE)
        val nextTotalMin = nextHour * 60 + nextMin
        val startTotalMin = startHour * 60 + startMinute
        val endTotalMin = endHour * 60 + endMinute

        if (nextTotalMin < startTotalMin || nextTotalMin > endTotalMin) {
            next.apply {
                if (nextTotalMin > endTotalMin) add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, startMinute)
                set(Calendar.SECOND, 0)
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent
        )
    }

    fun cancel(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    fun saveSettings(
        context: Context,
        intervalMinutes: Int,
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int,
        sound: Boolean, vibration: Boolean, overlay: Boolean,
        active: Boolean
    ) {
        getPrefs(context).edit().apply {
            putInt(KEY_INTERVAL_MINUTES, intervalMinutes)
            putInt(KEY_START_HOUR, startHour)
            putInt(KEY_START_MINUTE, startMinute)
            putInt(KEY_END_HOUR, endHour)
            putInt(KEY_END_MINUTE, endMinute)
            putBoolean(KEY_SOUND, sound)
            putBoolean(KEY_VIBRATION, vibration)
            putBoolean(KEY_OVERLAY, overlay)
            putBoolean(KEY_ACTIVE, active)
            apply()
        }
    }

    fun isActive(context: Context) = getPrefs(context).getBoolean(KEY_ACTIVE, false)
    fun getIntervalMinutes(context: Context) = getPrefs(context).getInt(KEY_INTERVAL_MINUTES, 60)
    fun getStartHour(context: Context) = getPrefs(context).getInt(KEY_START_HOUR, 8)
    fun getStartMinute(context: Context) = getPrefs(context).getInt(KEY_START_MINUTE, 0)
    fun getEndHour(context: Context) = getPrefs(context).getInt(KEY_END_HOUR, 22)
    fun getEndMinute(context: Context) = getPrefs(context).getInt(KEY_END_MINUTE, 0)
    fun isSoundEnabled(context: Context) = getPrefs(context).getBoolean(KEY_SOUND, true)
    fun isVibrationEnabled(context: Context) = getPrefs(context).getBoolean(KEY_VIBRATION, true)
    fun isOverlayEnabled(context: Context) = getPrefs(context).getBoolean(KEY_OVERLAY, true)

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
