package com.example.pomodorotimer

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.materialdesign.pomodoroapp.R
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var textTimer: TextView
    private lateinit var textSessionType: TextView
    private lateinit var textCompletedSessions: TextView
    private lateinit var textDailyGoal: TextView
    private lateinit var buttonStartPause: MaterialButton
    private lateinit var buttonReset: MaterialButton
    private lateinit var fabSettings: FloatingActionButton
    private lateinit var fabStats: FloatingActionButton
    private lateinit var progressDailyGoal: android.widget.ProgressBar
    private lateinit var textMotivation: TextView

    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 0
    private var originalTimeInMillis: Long = 0
    private var isBreak = false
    private var completedSessions = 0

    private var focusDuration = 25 * 60 * 1000L // 25 minutes default
    private var breakDuration = 5 * 60 * 1000L  // 5 minutes default
    private var dailyGoal = 8 // 8 sessions default

    private var mediaPlayer: MediaPlayer? = null
    private var backgroundMusicPlayer: MediaPlayer? = null

    // Motivational quotes - moved to strings.xml
    private lateinit var motivationalQuotes: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        loadSettings()
        motivationalQuotes = resources.getStringArray(R.array.motivational_quotes)
        updateTimerText()
        updateDailyGoal()
        createNotificationChannel()
        setRandomMotivationalQuote()

        buttonStartPause.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        buttonReset.setOnClickListener {
            resetTimer()
        }

        fabSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        fabStats.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initViews() {
        textTimer = findViewById(R.id.text_timer)
        textSessionType = findViewById(R.id.text_session_type)
        textCompletedSessions = findViewById(R.id.text_completed_sessions)
        textDailyGoal = findViewById(R.id.text_daily_goal)
        buttonStartPause = findViewById(R.id.button_start_pause)
        buttonReset = findViewById(R.id.button_reset)
        fabSettings = findViewById(R.id.fab_settings)
        fabStats = findViewById(R.id.fab_stats)
        progressDailyGoal = findViewById(R.id.progress_daily_goal)
        textMotivation = findViewById(R.id.text_motivation)
    }

    private fun loadSettings() {
        val sharedPreferences = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        focusDuration = sharedPreferences.getInt("focusDuration", 25) * 60 * 1000L
        breakDuration = sharedPreferences.getInt("breakDuration", 5) * 60 * 1000L
        dailyGoal = sharedPreferences.getInt("dailyGoal", 8)

        // Load today's completed sessions
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val lastRecordedDay = sharedPreferences.getInt("lastRecordedDay", -1)
        val lastRecordedYear = sharedPreferences.getInt("lastRecordedYear", -1)

        if (lastRecordedDay == today && lastRecordedYear == year) {
            completedSessions = sharedPreferences.getInt("todayCompletedSessions", 0)
        } else {
            // Reset for a new day
            completedSessions = 0
            with(sharedPreferences.edit()) {
                putInt("todayCompletedSessions", 0)
                putInt("lastRecordedDay", today)
                putInt("lastRecordedYear", year)
                apply()
            }
        }

        resetTimer()
    }

    private fun startTimer() {
        timeLeftInMillis = if (timeLeftInMillis == 0L) {
            if (!isBreak) focusDuration else breakDuration
        } else {
            timeLeftInMillis
        }
        originalTimeInMillis = if (!isBreak) focusDuration else breakDuration

        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                if (!isBreak) {
                    // Completed a focus session
                    completedSessions++
                    updateCompletedSessions()
                    saveStatistics()

                    // Show notification
                    showNotification(getString(R.string.focus_completed), getString(R.string.time_for_break))
                    playSound()

                    // Switch to break
                    isBreak = true
                    textSessionType.text = getString(R.string.break_session)
                } else {
                    // Break finished, back to focus
                    isBreak = false
                    textSessionType.text = getString(R.string.focus_session)
                    showNotification(getString(R.string.break_completed), getString(R.string.ready_for_focus))
                    playSound()
                }

                // Set new time based on session type
                resetTimer()
                startTimer() // Auto start the next session
            }
        }.start()

        isTimerRunning = true
        buttonStartPause.text = getString(R.string.pause)

        // Start background music if enabled
        if (getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE).getBoolean("backgroundMusic", false) && !isBreak) {
            startBackgroundMusic()
        }
    }

    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        buttonStartPause.text = getString(R.string.resume)
        stopBackgroundMusic()
    }

    private fun resetTimer() {
        timer?.cancel()
        timeLeftInMillis = if (!isBreak) focusDuration else breakDuration
        updateTimerText()
        isTimerRunning = false
        buttonStartPause.text = getString(R.string.start)
        stopBackgroundMusic()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        textTimer.text = timeFormatted
    }

    private fun updateDailyGoal() {
        textDailyGoal.text = dailyGoal.toString()
        progressDailyGoal.max = dailyGoal
        updateCompletedSessions()
    }

    private fun updateCompletedSessions() {
        textCompletedSessions.text = completedSessions.toString()
        progressDailyGoal.progress = completedSessions

        // Save to shared preferences
        val sharedPreferences = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("todayCompletedSessions", completedSessions)
            apply()
        }
    }

    private fun saveStatistics() {
        val sharedPreferences = getSharedPreferences("PomodoroStatistics", Context.MODE_PRIVATE)

        // Save total sessions
        val totalSessions = sharedPreferences.getInt("totalSessions", 0) + 1

        // Save week data
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val weekSessionsKey = "weekSessions_$dayOfWeek"
        val weekSessions = sharedPreferences.getInt(weekSessionsKey, 0) + 1

        // Save total focus time
        val totalFocusMinutes = sharedPreferences.getInt("totalFocusMinutes", 0) + (focusDuration / 60000).toInt()

        // Save streaks
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val lastActiveDay = sharedPreferences.getInt("lastActiveDay", -1)
        val currentStreak = if (lastActiveDay == today - 1 || lastActiveDay == -1) {
            sharedPreferences.getInt("currentStreak", 0) + 1
        } else if (lastActiveDay != today) {
            1
        } else {
            sharedPreferences.getInt("currentStreak", 0)
        }

        with(sharedPreferences.edit()) {
            putInt("totalSessions", totalSessions)
            putInt(weekSessionsKey, weekSessions)
            putInt("totalFocusMinutes", totalFocusMinutes)
            putInt("lastActiveDay", today)
            putInt("currentStreak", currentStreak)


            val bestStreak = sharedPreferences.getInt("bestStreak", 0)
            if (currentStreak > bestStreak) {
                putInt("bestStreak", currentStreak)
            }

            apply()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = "Notifications for Pomodoro Timer"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("POMODORO", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        if (getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE).getBoolean("notifications", true)) {
            val builder = NotificationCompat.Builder(this, "POMODORO")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(this)) {
                try {
                    notify(1, builder.build())
                } catch (e: SecurityException) {

                }
            }
        }
    }

    private fun playSound() {
        if (getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE).getBoolean("sound", true)) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.timer_sound)
            }
            mediaPlayer?.start()
        }
    }

    private fun startBackgroundMusic() {
        if (backgroundMusicPlayer == null) {
            backgroundMusicPlayer = MediaPlayer.create(this, R.raw.background_music)
            backgroundMusicPlayer?.isLooping = true
        }
        backgroundMusicPlayer?.start()
    }

    private fun stopBackgroundMusic() {
        backgroundMusicPlayer?.pause()
    }

    private fun setRandomMotivationalQuote() {
        val randomIndex = Random.nextInt(motivationalQuotes.size)
        textMotivation.text = motivationalQuotes[randomIndex]
    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences = getSharedPreferences("PomodoroTimer", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putLong("timeLeftInMillis", timeLeftInMillis)
            putBoolean("isTimerRunning", isTimerRunning)
            putBoolean("isBreak", isBreak)
            apply()
        }
    }

    override fun onResume() {
        super.onResume()

        loadSettings()
        updateDailyGoal()


        val sharedPreferences = getSharedPreferences("PomodoroTimer", Context.MODE_PRIVATE)
        val savedTime = sharedPreferences.getLong("timeLeftInMillis", 0L)
        val savedIsRunning = sharedPreferences.getBoolean("isTimerRunning", false)
        val savedIsBreak = sharedPreferences.getBoolean("isBreak", false)

        if (savedTime > 0) {
            timeLeftInMillis = savedTime
            isBreak = savedIsBreak
            textSessionType.text = if (isBreak) getString(R.string.break_session) else getString(R.string.focus_session)
            updateTimerText()

            if (savedIsRunning) {
                startTimer()
            }
        }


        setRandomMotivationalQuote()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        backgroundMusicPlayer?.release()
        backgroundMusicPlayer = null
    }
}