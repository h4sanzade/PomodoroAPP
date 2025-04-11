package com.example.pomodorotimer

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var seekbarFocusDuration: SeekBar
    private lateinit var seekbarBreakDuration: SeekBar
    private lateinit var seekbarDailyGoal: SeekBar
    private lateinit var textFocusDuration: TextView
    private lateinit var textBreakDuration: TextView
    private lateinit var textDailyGoalValue: TextView
    private lateinit var switchDarkMode: SwitchCompat
    private lateinit var switchSound: SwitchCompat
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var switchBackgroundMusic: SwitchCompat
    private lateinit var buttonSaveSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        loadCurrentSettings()
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        seekbarFocusDuration = findViewById(R.id.seekbar_focus_duration)
        seekbarBreakDuration = findViewById(R.id.seekbar_break_duration)
        seekbarDailyGoal = findViewById(R.id.seekbar_daily_goal)
        textFocusDuration = findViewById(R.id.text_focus_duration)
        textBreakDuration = findViewById(R.id.text_break_duration)
        textDailyGoalValue = findViewById(R.id.text_daily_goal_value)
        switchDarkMode = findViewById(R.id.switch_dark_mode)
        switchSound = findViewById(R.id.switch_sound)
        switchNotifications = findViewById(R.id.switch_notifications)
        switchBackgroundMusic = findViewById(R.id.switch_background_music)
        buttonSaveSettings = findViewById(R.id.button_save_settings)
    }

    private fun loadCurrentSettings() {
        val sharedPreferences = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)

        // Load timer duration settings
        val focusDuration = sharedPreferences.getInt("focusDuration", 25)
        val breakDuration = sharedPreferences.getInt("breakDuration", 5)
        val dailyGoal = sharedPreferences.getInt("dailyGoal", 8)

        seekbarFocusDuration.progress = focusDuration
        seekbarBreakDuration.progress = breakDuration
        seekbarDailyGoal.progress = dailyGoal

        textFocusDuration.text = focusDuration.toString()
        textBreakDuration.text = breakDuration.toString()
        textDailyGoalValue.text = dailyGoal.toString()

        // Load appearance and sound settings
        switchDarkMode.isChecked = sharedPreferences.getBoolean("darkMode", false)
        switchSound.isChecked = sharedPreferences.getBoolean("sound", true)
        switchNotifications.isChecked = sharedPreferences.getBoolean("notifications", true)
        switchBackgroundMusic.isChecked = sharedPreferences.getBoolean("backgroundMusic", false)
    }

    private fun setupListeners() {
        // Focus Duration SeekBar
        seekbarFocusDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textFocusDuration.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Break Duration SeekBar
        seekbarBreakDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textBreakDuration.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Daily Goal SeekBar
        seekbarDailyGoal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textDailyGoalValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Dark Mode Switch
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Save Button
        buttonSaveSettings.setOnClickListener {
            saveSettings()
            finish()
        }

        // Toolbar back button
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun saveSettings() {
        val sharedPreferences = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            // Save timer durations
            putInt("focusDuration", seekbarFocusDuration.progress)
            putInt("breakDuration", seekbarBreakDuration.progress)
            putInt("dailyGoal", seekbarDailyGoal.progress)

            // Save appearance and sound settings
            putBoolean("darkMode", switchDarkMode.isChecked)
            putBoolean("sound", switchSound.isChecked)
            putBoolean("notifications", switchNotifications.isChecked)
            putBoolean("backgroundMusic", switchBackgroundMusic.isChecked)

            apply()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}