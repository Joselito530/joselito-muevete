package com.example.joselitomueveteomuere

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OverlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
        setContentView(R.layout.activity_overlay)
        findViewById<TextView>(R.id.tvExercise).text = ExerciseManager.getCurrentExercise(this)
        findViewById<Button>(R.id.btnDone).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSnooze).setOnClickListener {
            AlarmScheduler.snooze(this)
            finish()
        }
    }
}
