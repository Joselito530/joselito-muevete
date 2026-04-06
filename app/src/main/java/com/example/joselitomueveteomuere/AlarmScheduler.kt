package com.example.joselitomueveteomuere

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import java.util.Calendar

object AlarmScheduler {

    private const val PREFS_NAME = "joselito_prefs"
    const val REQUEST_CODE = 1001

    fun scheduleNext(context: Context) {
        val prefs = getPrefs(context)
        if (!prefs.getBoolean("alarm_active", false)) return
        val intervalMinutes = prefs.getInt("interval_minutes", 60)
        val next = findNextValidTime(context, intervalMinutes) ?: return
        prefs.edit().putLong("next_alarm_time", next).apply()
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
            .setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi)
    }

    private fun findNextValidTime(context: Context, intervalMinutes: Int): Long? {
        val prefs = getPrefs(context)
        val candidate = Calendar.getInstance().apply {
            add(Calendar.MINUTE, intervalMinutes); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val s1h = prefs.getInt("start_hour1", 8); val s1m = prefs.getInt("start_minute1", 0)
        val e1h = prefs.getInt("end_hour1", 14); val e1m = prefs.getInt("end_minute1", 0)
        val s2h = prefs.getInt("start_hour2", 17); val s2m = prefs.getInt("start_minute2", 0)
        val e2h = prefs.getInt("end_hour2", 22); val e2m = prefs.getInt("end_minute2", 0)
        val psh = prefs.getInt("pause_start_hour", 14); val psm = prefs.getInt("pause_start_minute", 0)
        val peh = prefs.getInt("pause_end_hour", 17); val pem = prefs.getInt("pause_end_minute", 0)
        fun tm(h: Int, m: Int) = h * 60 + m
        val s1 = tm(s1h,s1m); val e1 = tm(e1h,e1m); val s2 = tm(s2h,s2m)
        val e2 = tm(e2h,e2m); val ps = tm(psh,psm); val pe = tm(peh,pem)
        fun isValid(cal: Calendar): Boolean {
            val t = tm(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            return (t in s1..e1 || t in s2..e2) && t !in ps..pe
        }
        if (isValid(candidate)) return candidate.timeInMillis
        val now = System.currentTimeMillis()
        listOf(
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,s1h); set(Calendar.MINUTE,s1m); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0) },
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,s2h); set(Calendar.MINUTE,s2m); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0) },
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR,1); set(Calendar.HOUR_OF_DAY,s1h); set(Calendar.MINUTE,s1m); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0) }
        ).forEach { if (it.timeInMillis > now) return it.timeInMillis }
        return null
    }

    fun cancel(context: Context) {
        getPrefs(context).edit().remove("next_alarm_time").apply()
        val pi = PendingIntent.getBroadcast(context, REQUEST_CODE,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi)
    }

    fun getNextAlarmTime(context: Context): Long? {
        val t = getPrefs(context).getLong("next_alarm_time", 0L)
        return if (t > System.currentTimeMillis()) t else null
    }

    fun saveSettings(context: Context, intervalMinutes: Int, sound: Boolean,
                     vibration: Boolean, overlay: Boolean, active: Boolean) {
        getPrefs(context).edit().apply {
            putInt("interval_minutes", intervalMinutes)
            putBoolean("sound_enabled", sound); putBoolean("vibration_enabled", vibration)
            putBoolean("overlay_enabled", overlay); putBoolean("alarm_active", active)
            apply()
        }
    }

    fun isActive(context: Context) = getPrefs(context).getBoolean("alarm_active", false)
    fun getIntervalMinutes(context: Context) = getPrefs(context).getInt("interval_minutes", 60)
    fun isSoundEnabled(context: Context) = getPrefs(context).getBoolean("sound_enabled", true)
    fun isVibrationEnabled(context: Context) = getPrefs(context).getBoolean("vibration_enabled", true)
    fun isOverlayEnabled(context: Context) = getPrefs(context).getBoolean("overlay_enabled", true)
    fun getStartHour1(context: Context) = getPrefs(context).getInt("start_hour1", 8)
    fun getStartMinute1(context: Context) = getPrefs(context).getInt("start_minute1", 0)
    fun getEndHour1(context: Context) = getPrefs(context).getInt("end_hour1", 14)
    fun getEndMinute1(context: Context) = getPrefs(context).getInt("end_minute1", 0)
    fun getStartHour2(context: Context) = getPrefs(context).getInt("start_hour2", 17)
    fun getStartMinute2(context: Context) = getPrefs(context).getInt("start_minute2", 0)
    fun getEndHour2(context: Context) = getPrefs(context).getInt("end_hour2", 22)
    fun getEndMinute2(context: Context) = getPrefs(context).getInt("end_minute2", 0)
    fun getPauseStartHour(context: Context) = getPrefs(context).getInt("pause_start_hour", 14)
    fun getPauseStartMinute(context: Context) = getPrefs(context).getInt("pause_start_minute", 0)
    fun getPauseEndHour(context: Context) = getPrefs(context).getInt("pause_end_hour", 17)
    fun getPauseEndMinute(context: Context) = getPrefs(context).getInt("pause_end_minute", 0)

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
