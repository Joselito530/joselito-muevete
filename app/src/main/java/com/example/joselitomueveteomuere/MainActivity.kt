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
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var seekInterval: SeekBar
    private lateinit var tvIntervalLabel: TextView
    private lateinit var switchSound: Switch
    private lateinit var switchVibration: Switch
    private lateinit var switchOverlay: Switch
    private lateinit var btnToggle: Button
    private lateinit var spinnerExercise: Spinner

    private var startHour = 8
    private var startMinute = 0
    private var endHour = 22
    private var endMinute = 0
    private var intervalMinutes = 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        loadSettings()
        setupListeners()
        requestPermissions()
    }

    private fun initViews() {
        tvStartTime = findViewById(R.id.tvStartTime)
        tvEndTime = findViewById(R.id.tvEndTime)
        seekInterval = findViewById(R.id.seekInterval)
        tvIntervalLabel = findViewById(R.id.tvIntervalLabel)
        switchSound = findViewById(R.id.switchSound)
        switchVibration = findViewById(R.id.switchVibration)
        switchOverlay = findViewById(R.id.switchOverlay)
        btnToggle = findViewById(R.id.btnToggle)
        spinnerExercise = findViewById(R.id.spinnerExercise)
    }

    private fun loadSettings() {
        startHour = AlarmScheduler.getStartHour(this)
        startMinute = AlarmScheduler.getStartMinute(this)
        endHour = AlarmScheduler.getEndHour(this)
        endMinute = AlarmScheduler.getEndMinute(this)
        intervalMinutes = AlarmScheduler.getIntervalMinutes(this)

        updateStartTimeLabel()
        updateEndTimeLabel()

        seekInterval.max = 21
        seekInterval.progress = ((intervalMinutes - 15) / 5).coerceIn(0, 21)
        updateIntervalLabel(intervalMinutes)

        switchSound.isChecked = AlarmScheduler.isSoundEnabled(this)
        switchVibration.isChecked = AlarmScheduler.isVibrationEnabled(this)
        switchOverlay.isChecked = AlarmScheduler.isOverlayEnabled(this)

        val exercises = ExerciseManager.getExercises(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exercises)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerExercise.adapter = adapter
        spinnerExercise.setSelection(ExerciseManager.getSelectedIndex(this))

        updateToggleButton()
    }

    private fun setupListeners() {
        tvStartTime.setOnClickListener { showTimePicker(true) }
        tvEndTime.setOnClickListener { showTimePicker(false) }

        seekInterval.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                intervalMinutes = 15 + progress * 5
                updateIntervalLabel(intervalMinutes)
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        spinnerExercise.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                ExerciseManager.setSelectedIndex(this@MainActivity, pos)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnToggle.setOnClickListener {
            val isActive = AlarmScheduler.isActive(this)
            saveCurrentSettings(!isActive)
            if (!isActive) {
                AlarmScheduler.scheduleNext(this)
                Toast.makeText(this, "Joselito activado! ", Toast.LENGTH_SHORT).show()
            } else {
                AlarmScheduler.cancel(this)
                Toast.makeText(this, "Joselito en reposo", Toast.LENGTH_SHORT).show()
            }
            updateToggleButton()
        }
    }

    private fun showTimePicker(isStart: Boolean) {
        val h = if (isStart) startHour else endHour
        val m = if (isStart) startMinute else endMinute
        TimePickerDialog(this, { _, hour, minute ->
            if (isStart) { startHour = hour; startMinute = minute; updateStartTimeLabel() }
            else { endHour = hour; endMinute = minute; updateEndTimeLabel() }
        }, h, m, true).show()
    }

    private fun updateStartTimeLabel() {
        tvStartTime.text = String.format("Inicio: %02d:%02d", startHour, startMinute)
    }

    private fun updateEndTimeLabel() {
        tvEndTime.text = String.format("Fin: %02d:%02d", endHour, endMinute)
    }

    private fun updateIntervalLabel(minutes: Int) {
        tvIntervalLabel.text = when {
            minutes < 60 -> "Cada $minutes minutos"
            minutes == 60 -> "Cada hora"
            else -> "Cada ${minutes / 60}h ${if (minutes % 60 > 0) "${minutes % 60}min" else ""}"
        }
    }

    private fun updateToggleButton() {
        val active = AlarmScheduler.isActive(this)
        btnToggle.text = if (active) "DESACTIVAR" else "ACTIVAR"
        btnToggle.setBackgroundColor(
            if (active) getColor(R.color.color_stop) else getColor(R.color.color_start)
        )
    }

    private fun saveCurrentSettings(active: Boolean) {
        AlarmScheduler.saveSettings(
            this, intervalMinutes,
            startHour, startMinute, endHour, endMinute,
            switchSound.isChecked, switchVibration.isChecked, switchOverlay.isChecked, active
        )
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
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
}
