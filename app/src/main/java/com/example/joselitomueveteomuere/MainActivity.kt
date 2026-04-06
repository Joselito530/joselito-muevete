package com.example.joselitomueveteomuere

import android.Manifest
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvNextAlarm: TextView
    private lateinit var tvCurrentExercise: TextView
    private lateinit var tvStartTime1: TextView
    private lateinit var tvEndTime1: TextView
    private lateinit var tvStartTime2: TextView
    private lateinit var tvEndTime2: TextView
    private lateinit var tvPauseStart: TextView
    private lateinit var tvPauseEnd: TextView
    private lateinit var seekInterval: SeekBar
    private lateinit var tvIntervalLabel: TextView
    private lateinit var switchSound: Switch
    private lateinit var switchVibration: Switch
    private lateinit var switchOverlay: Switch
    private lateinit var btnToggle: Button
    private lateinit var btnEditExercises: Button

    private var intervalMinutes = 60
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        loadSettings()
        setupListeners()
        requestPermissions()
        updateCurrentExercise()
        startCountDown()
    }

    override fun onResume() {
        super.onResume()
        updateCurrentExercise()
        startCountDown()
    }

    private fun initViews() {
        tvNextAlarm = findViewById(R.id.tvNextAlarm)
        tvCurrentExercise = findViewById(R.id.tvCurrentExercise)
        tvStartTime1 = findViewById(R.id.tvStartTime1)
        tvEndTime1 = findViewById(R.id.tvEndTime1)
        tvStartTime2 = findViewById(R.id.tvStartTime2)
        tvEndTime2 = findViewById(R.id.tvEndTime2)
        tvPauseStart = findViewById(R.id.tvPauseStart)
        tvPauseEnd = findViewById(R.id.tvPauseEnd)
        seekInterval = findViewById(R.id.seekInterval)
        tvIntervalLabel = findViewById(R.id.tvIntervalLabel)
        switchSound = findViewById(R.id.switchSound)
        switchVibration = findViewById(R.id.switchVibration)
        switchOverlay = findViewById(R.id.switchOverlay)
        btnToggle = findViewById(R.id.btnToggle)
        btnEditExercises = findViewById(R.id.btnEditExercises)
    }

    private fun loadSettings() {
        val s = AlarmScheduler
        updateTimeLabel(tvStartTime1, "Inicio", s.getStartHour1(this), s.getStartMinute1(this))
        updateTimeLabel(tvEndTime1, "Fin", s.getEndHour1(this), s.getEndMinute1(this))
        updateTimeLabel(tvStartTime2, "Inicio", s.getStartHour2(this), s.getStartMinute2(this))
        updateTimeLabel(tvEndTime2, "Fin", s.getEndHour2(this), s.getEndMinute2(this))
        updateTimeLabel(tvPauseStart, "Pausa desde", s.getPauseStartHour(this), s.getPauseStartMinute(this))
        updateTimeLabel(tvPauseEnd, "hasta", s.getPauseEndHour(this), s.getPauseEndMinute(this))
        intervalMinutes = s.getIntervalMinutes(this)
        seekInterval.max = 21
        seekInterval.progress = ((intervalMinutes - 15) / 5).coerceIn(0, 21)
        updateIntervalLabel(intervalMinutes)
        switchSound.isChecked = s.isSoundEnabled(this)
        switchVibration.isChecked = s.isVibrationEnabled(this)
        switchOverlay.isChecked = s.isOverlayEnabled(this)
        updateToggleButton()
    }

    private fun setupListeners() {
        tvStartTime1.setOnClickListener { showTimePicker("start1") }
        tvEndTime1.setOnClickListener { showTimePicker("end1") }
        tvStartTime2.setOnClickListener { showTimePicker("start2") }
        tvEndTime2.setOnClickListener { showTimePicker("end2") }
        tvPauseStart.setOnClickListener { showTimePicker("pauseStart") }
        tvPauseEnd.setOnClickListener { showTimePicker("pauseEnd") }

        seekInterval.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                intervalMinutes = 15 + progress * 5
                updateIntervalLabel(intervalMinutes)
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        btnToggle.setOnClickListener {
            val isActive = AlarmScheduler.isActive(this)
            saveCurrentSettings(!isActive)
            if (!isActive) {
                AlarmScheduler.scheduleNext(this)
                Toast.makeText(this, "Joselito activado!", Toast.LENGTH_SHORT).show()
                startCountDown()
            } else {
                AlarmScheduler.cancel(this)
                countDownTimer?.cancel()
                tvNextAlarm.text = "Desactivado"
                Toast.makeText(this, "Joselito en reposo", Toast.LENGTH_SHORT).show()
            }
            updateToggleButton()
        }

        btnEditExercises.setOnClickListener {
            startActivity(Intent(this, ExerciseListActivity::class.java))
        }
    }

    private fun showTimePicker(field: String) {
        val s = AlarmScheduler
        val (h, m) = when (field) {
            "start1" -> Pair(s.getStartHour1(this), s.getStartMinute1(this))
            "end1" -> Pair(s.getEndHour1(this), s.getEndMinute1(this))
            "start2" -> Pair(s.getStartHour2(this), s.getStartMinute2(this))
            "end2" -> Pair(s.getEndHour2(this), s.getEndMinute2(this))
            "pauseStart" -> Pair(s.getPauseStartHour(this), s.getPauseStartMinute(this))
            "pauseEnd" -> Pair(s.getPauseEndHour(this), s.getPauseEndMinute(this))
            else -> Pair(8, 0)
        }
        TimePickerDialog(this, { _, hour, minute ->
            val prefs = getSharedPreferences("joselito_prefs", Context.MODE_PRIVATE).edit()
            when (field) {
                "start1" -> { prefs.putInt("start_hour1", hour); prefs.putInt("start_minute1", minute)
                    updateTimeLabel(tvStartTime1, "Inicio", hour, minute) }
                "end1" -> { prefs.putInt("end_hour1", hour); prefs.putInt("end_minute1", minute)
                    updateTimeLabel(tvEndTime1, "Fin", hour, minute) }
                "start2" -> { prefs.putInt("start_hour2", hour); prefs.putInt("start_minute2", minute)
                    updateTimeLabel(tvStartTime2, "Inicio", hour, minute) }
                "end2" -> { prefs.putInt("end_hour2", hour); prefs.putInt("end_minute2", minute)
                    updateTimeLabel(tvEndTime2, "Fin", hour, minute) }
                "pauseStart" -> { prefs.putInt("pause_start_hour", hour); prefs.putInt("pause_start_minute", minute)
                    updateTimeLabel(tvPauseStart, "Pausa desde", hour, minute) }
                "pauseEnd" -> { prefs.putInt("pause_end_hour", hour); prefs.putInt("pause_end_minute", minute)
                    updateTimeLabel(tvPauseEnd, "hasta", hour, minute) }
            }
            prefs.apply()
        }, h, m, true).show()
    }

    private fun updateTimeLabel(tv: TextView, prefix: String, h: Int, m: Int) {
        tv.text = "$prefix: %02d:%02d".format(h, m)
    }

    private fun updateIntervalLabel(minutes: Int) {
        tvIntervalLabel.text = when {
            minutes < 60 -> "Cada $minutes min"
            minutes == 60 -> "Cada hora"
            else -> "Cada ${minutes/60}h ${if(minutes%60>0) "${minutes%60}min" else ""}"
        }
    }

    private fun updateToggleButton() {
        val active = AlarmScheduler.isActive(this)
        btnToggle.text = if (active) "DESACTIVAR" else "ACTIVAR"
        btnToggle.setBackgroundColor(
            if (active) getColor(R.color.color_stop) else getColor(R.color.color_start)
        )
    }

    private fun updateCurrentExercise() {
        tvCurrentExercise.text = ExerciseManager.getCurrentExercise(this)
    }

    private fun startCountDown() {
        countDownTimer?.cancel()
        val nextTime = AlarmScheduler.getNextAlarmTime(this) ?: return
        val remaining = nextTime - System.currentTimeMillis()
        if (remaining <= 0) return
        countDownTimer = object : CountDownTimer(remaining, 1000) {
            override fun onTick(ms: Long) {
                val h = ms / 3600000; val m = (ms % 3600000) / 60000; val s = (ms % 60000) / 1000
                tvNextAlarm.text = "Proximo aviso: %02d:%02d:%02d".format(h, m, s)
            }
            override fun onFinish() { tvNextAlarm.text = "Avisando..." }
        }.start()
    }

    private fun saveCurrentSettings(active: Boolean) {
        AlarmScheduler.saveSettings(this, intervalMinutes,
            switchSound.isChecked, switchVibration.isChecked, switchOverlay.isChecked, active)
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(this)
                    .setTitle("Permiso necesario")
                    .setMessage("Para avisos puntuales, permite las alarmas exactas.")
                    .setPositiveButton("Abrir ajustes") { _, _ ->
                        startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:$packageName")))
                    }
                    .setNegativeButton("Ahora no", null).show()
            }
        }
    }

    override fun onDestroy() { super.onDestroy(); countDownTimer?.cancel() }
}
