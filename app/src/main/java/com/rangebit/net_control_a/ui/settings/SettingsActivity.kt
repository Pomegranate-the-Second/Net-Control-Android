package com.rangebit.net_control_a.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.rangebit.net_control_a.R
import com.rangebit.net_control_a.ui.map.MapActivity
import androidx.core.content.edit

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var switchMode: SwitchCompat
    private lateinit var btnLoadData: Button
    private lateinit var etAnalytics: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        btnBack = findViewById(R.id.btnBack)
        switchMode = findViewById(R.id.switchMode)
        btnLoadData = findViewById(R.id.btnLoadData)
        etAnalytics = findViewById(R.id.etAnalytics)

        val isProfessional = prefs.getBoolean("professional_mode", false)
        switchMode.isChecked = isProfessional

        val analyticsText = prefs.getString("analytics_text", "rangebit.top")
        if (analyticsText.isNullOrEmpty()) {etAnalytics.setText(analyticsText)}

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        switchMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("professional_mode", isChecked) }
            if (isChecked) {
                // Профессиональный режим включен
                Toast.makeText(this, "Профессиональный режим включен", Toast.LENGTH_SHORT).show()
            } else {
                // Профессиональный режим выключен
                Toast.makeText(this, "Профессиональный режим выключен", Toast.LENGTH_SHORT).show()
            }
        }

        btnLoadData.setOnClickListener {
            val text = etAnalytics.text.toString()
            prefs.edit { putString("analytics_text", text) }

            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
        }



    }
}