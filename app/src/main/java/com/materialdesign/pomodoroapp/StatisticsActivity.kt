package com.example.pomodorotimer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var textTotalFocusTime: TextView
    private lateinit var textTodaySessions: TextView
    private lateinit var textWeekSessions: TextView
    private lateinit var textTotalSessions: TextView
    private lateinit var textDailyStreak: TextView
    private lateinit var textMostProductiveDay: TextView
    private lateinit var textMostProductiveTime: TextView
    private lateinit var buttonExportStatistics: Button
    private lateinit var textNoAchievements: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        initViews()
        loadStatistics()
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        textTotalFocusTime = findViewById(R.id.text_total_focus_time)
        textTodaySessions = findViewById(R.id.text_today_sessions)
        textWeekSessions = findViewById(R.id.text_week_sessions)
        textTotalSessions = findViewById(R.id.text_total_sessions)
        textDailyStreak = findViewById(R.id.text_daily_streak)
        textMostProductiveDay = findViewById(R.id.text_most_productive_day)
        textMostProductiveTime = findViewById(R.id.text_most_productive_time)
        buttonExportStatistics = findViewById(R.id.button_export_statistics)
        textNoAchievements = findViewById(R.id.text_no_achievements)

        // Show no achievements text initially
        textNoAchievements.visibility = View.VISIBLE
    }

    private fun loadStatistics() {
        val sharedPreferences = getSharedPreferences("PomodoroStatistics", Context.MODE_PRIVATE)
        val settingsPrefs = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)

        // Total focus time
        val totalFocusMinutes = sharedPreferences.getInt("totalFocusMinutes", 0)
        val hours = totalFocusMinutes / 60
        val minutes = totalFocusMinutes % 60
        textTotalFocusTime.text = "${hours}h ${minutes}m"

        // Sessions stats
        val todaySessions = settingsPrefs.getInt("todayCompletedSessions", 0)
        textTodaySessions.text = todaySessions.toString()

        // Calculate week sessions
        var weekSessionsTotal = 0
        for (i in 0..6) {
            weekSessionsTotal += sharedPreferences.getInt("weekSessions_$i", 0)
        }
        textWeekSessions.text = weekSessionsTotal.toString()

        // Total sessions
        val totalSessions = sharedPreferences.getInt("totalSessions", 0)
        textTotalSessions.text = totalSessions.toString()

        // Daily streak
        val currentStreak = sharedPreferences.getInt("currentStreak", 0)
        textDailyStreak.text = currentStreak.toString()

        // Most productive day and time
        determineMostProductiveDay()
        determineMostProductiveTime()

        // Update weekly chart
        updateWeeklyChart()

        // Check achievements
        checkAchievements(totalFocusMinutes, totalSessions, currentStreak)
    }

    private fun determineMostProductiveDay() {
        val sharedPreferences = getSharedPreferences("PomodoroStatistics", Context.MODE_PRIVATE)

        // Find day with most sessions
        var maxSessions = 0
        var maxDay = -1

        for (i in 0..6) {
            val sessions = sharedPreferences.getInt("weekSessions_$i", 0)
            if (sessions > maxSessions) {
                maxSessions = sessions
                maxDay = i
            }
        }

        // Convert day index to day name
        val dayName = when(maxDay) {
            0 -> "Sunday"
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            else -> "-"
        }

        textMostProductiveDay.text = if (maxDay >= 0) dayName else "-"
    }

    private fun determineMostProductiveTime() {
        // In a real app, you would track sessions with timestamps
        // For simplicity, we'll use a placeholder
        textMostProductiveTime.text = "Morning"
    }

    private fun updateWeeklyChart() {
        // This would update the chart view with actual data
        // For a real app, you'd manipulate the view heights based on data
    }

    private fun checkAchievements(totalMinutes: Int, totalSessions: Int, streak: Int) {
        var hasAchievements = false

        // In a real app, you would have a more sophisticated system
        if (totalSessions >= 50) {
            hasAchievements = true
            // Add achievement to list or display it
        }

        if (totalMinutes >= 500) {
            hasAchievements = true
            // Add achievement to list
        }

        if (streak >= 7) {
            hasAchievements = true
            // Add achievement for weekly streak
        }

        textNoAchievements.visibility = if (hasAchievements) View.GONE else View.VISIBLE
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        buttonExportStatistics.setOnClickListener {
            exportStatistics()
        }
    }

    private fun exportStatistics() {
        try {
            // Create statistics text
            val statistics = generateStatisticsText()

            // Create a file in the app's cache directory
            val fileName = "pomodoro_stats_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.txt"
            val file = File(cacheDir, fileName)

            // Write statistics to the file
            FileOutputStream(file).use {
                it.write(statistics.toByteArray())
            }

            // Create a content URI using FileProvider
            val contentUri = FileProvider.getUriForFile(
                this,
                "com.example.pomodorotimer.fileprovider",
                file
            )

            // Create a share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Start the share activity
            startActivity(Intent.createChooser(shareIntent, "Share Statistics"))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateStatisticsText(): String {
        val sb = StringBuilder()
        sb.appendLine("Pomodoro Timer Statistics")
        sb.appendLine("========================")
        sb.appendLine()
        sb.appendLine("Total Focus Time: ${textTotalFocusTime.text}")
        sb.appendLine("Total Sessions: ${textTotalSessions.text}")
        sb.appendLine("Current Streak: ${textDailyStreak.text} days")
        sb.appendLine()
        sb.appendLine("Sessions Today: ${textTodaySessions.text}")
        sb.appendLine("Sessions This Week: ${textWeekSessions.text}")
        sb.appendLine()
        sb.appendLine("Most Productive Day: ${textMostProductiveDay.text}")
        sb.appendLine("Most Productive Time: ${textMostProductiveTime.text}")
        sb.appendLine()
        sb.appendLine("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")

        return sb.toString()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}