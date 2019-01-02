package com.caughtknee.fastingassistant

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.text.InputType
import android.widget.TextView
import android.widget.Toast
import android.content.SharedPreferences
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import org.joda.time.LocalTime


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var editTextTimeStart: EditText
    private lateinit var editTextTimeEnd: EditText
    private lateinit var textViewStatus: TextView
    private lateinit var layoutStatus: ConstraintLayout
    private lateinit var adView : AdView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this, "ca-app-pub-0834215168797849~8574224905")
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        textViewStatus = findViewById(R.id.textViewStatus)
        layoutStatus = findViewById(R.id.layoutStatus)
        editTextTimeStart = findViewById(R.id.editTextTimeStart)
        editTextTimeEnd = findViewById(R.id.editTextTimeEnd)
        editTextTimeStart.setOnClickListener(this)
        editTextTimeEnd.setOnClickListener(this)
        editTextTimeStart.inputType = InputType.TYPE_NULL
        editTextTimeEnd.inputType = InputType.TYPE_NULL

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE) ?: return
        val hourStart = sharedPreferences.getInt(getString(R.string.saved_time_picker_start_hour), 7)
        val minuteStart = sharedPreferences.getInt(getString(R.string.saved_time_picker_start_minute), 0)
        val hourEnd = sharedPreferences.getInt(getString(R.string.saved_time_picker_end_hour), 19)
        val minuteEnd = sharedPreferences.getInt(getString(R.string.saved_time_picker_end_minute), 0)
        setTime(editTextTimeStart, hourStart, minuteStart)
        setTime(editTextTimeEnd, hourEnd, minuteEnd)
        setStatus()
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.editTextTimeStart -> {
                val is24Hour = sharedPreferences.getBoolean(getString(R.string.saved_time_picker_start_is_24_hour), true)
                val hourStart = sharedPreferences.getInt(getString(R.string.saved_time_picker_start_hour), 7)
                val minuteStart = sharedPreferences.getInt(getString(R.string.saved_time_picker_start_minute), 0)
                TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute -> onTimeSet(editTextTimeStart, hourOfDay, minute) }, hourStart, minuteStart, is24Hour).show()
            }
            R.id.editTextTimeEnd -> {
                val is24Hour = sharedPreferences.getBoolean(getString(R.string.saved_time_picker_end_is_24_hour), true)
                val hourEnd = sharedPreferences.getInt(getString(R.string.saved_time_picker_end_hour), 19)
                val minuteEnd = sharedPreferences.getInt(getString(R.string.saved_time_picker_end_minute), 0)
                TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute -> onTimeSet(editTextTimeEnd, hourOfDay, minute) }, hourEnd, minuteEnd, is24Hour).show()
            }
        }
    }

    private fun setTime(view: EditText, hourOfDay: Int, minute: Int) {
        view.setText(timeFormat(hourOfDay, minute), TextView.BufferType.EDITABLE)
    }

    private fun setStatus() {
        val hourStart = sharedPreferences.getInt(getString(R.string.saved_time_picker_start_hour), 7)
        val minuteStart = sharedPreferences.getInt(getString(R.string.saved_time_picker_start_minute), 0)
        val hourEnd = sharedPreferences.getInt(getString(R.string.saved_time_picker_end_hour), 19)
        val minuteEnd = sharedPreferences.getInt(getString(R.string.saved_time_picker_end_minute), 0)
        val eatingAllowed = canEat(LocalTime.now(), hourStart, minuteStart, hourEnd, minuteEnd)
        textViewStatus.setText(if (eatingAllowed) R.string.status_eating else R.string.status_fasting)
        layoutStatus.setBackgroundColor(ResourcesCompat.getColor(resources, if (eatingAllowed) R.color.colourEating else R.color.colourFasting, null))
    }

    private fun onTimeSet(view: EditText, hourOfDay: Int, minute: Int) {
        when(view.id) {
            R.id.editTextTimeStart -> {
                with (sharedPreferences.edit()) {
                    putInt(getString(R.string.saved_time_picker_start_hour), hourOfDay)
                    putInt(getString(R.string.saved_time_picker_start_minute), minute)
                    apply()
                }
            }
            R.id.editTextTimeEnd -> {
                with (sharedPreferences.edit()) {
                    putInt(getString(R.string.saved_time_picker_end_hour), hourOfDay)
                    putInt(getString(R.string.saved_time_picker_end_minute), minute)
                    apply()
                }
            }
        }
        setTime(view, hourOfDay, minute)
        setStatus()
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        setStatus()
    }

    override fun onPause() {
        super.onPause()

        // Update app widget
        val intent = Intent(this, MainWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(ComponentName(application, MainWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }
}