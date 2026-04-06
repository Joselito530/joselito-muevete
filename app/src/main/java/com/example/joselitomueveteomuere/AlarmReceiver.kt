package com.example.joselitomueveteomuere

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (AlarmScheduler.isActive(context)) AlarmScheduler.scheduleNext(context)
            return
        }
        ExerciseManager.advanceToNext(context)
        if (AlarmScheduler.isSoundEnabled(context)) playSound(context)
        if (AlarmScheduler.isVibrationEnabled(context)) vibrate(context)
        if (AlarmScheduler.isOverlayEnabled(context)) {
            context.startActivity(Intent(context, OverlayActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }
        AlarmScheduler.scheduleNext(context)
    }

    private fun playSound(context: Context) {
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (am.getStreamVolume(AudioManager.STREAM_ALARM) == 0)
                am.setStreamVolume(AudioManager.STREAM_ALARM,
                    am.getStreamMaxVolume(AudioManager.STREAM_ALARM) / 2, 0)
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                r?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            }
            r?.play()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun vibrate(context: Context) {
        try {
            val pattern = longArrayOf(0, 800, 300, 800, 300, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                    .defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    v.vibrate(VibrationEffect.createWaveform(pattern, -1))
                else @Suppress("DEPRECATION") v.vibrate(pattern, -1)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}
