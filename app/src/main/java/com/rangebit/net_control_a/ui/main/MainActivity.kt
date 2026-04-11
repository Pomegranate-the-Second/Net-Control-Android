package com.rangebit.net_control_a.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.Utils
import com.rangebit.net_control_a.R
import com.rangebit.net_control_a.data.source.location.LocationManager
import com.rangebit.net_control_a.data.source.network.MeasurementManager
import com.rangebit.net_control_a.ui.map.MapActivity
import com.rangebit.net_control_a.ui.measurement.MeasurementActivity
import com.rangebit.net_control_a.ui.settings.SettingsActivity
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(LocationManager())
    }

    private var cnt = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())

        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnStartTest).setOnClickListener {
            val intent = Intent(this, MeasurementActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btnAnalytics).setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        Timber.tag("TEST").d("Latitude: no, Longitude: no")

        observeViewModel()

        viewModel.handleIntent(AppIntent.StartLocating, this)


    }

    private fun observeViewModel() {

        lifecycleScope.launchWhenStarted {

            launch {
                viewModel.state.collect { state ->
                    when (state) {

                        is AppState.Idle -> {}

                        is AppState.Locating -> {
                            Timber.tag("LOCATION").d("Not in use! Latitude: ..., Longitude: ...")
                            Toast.makeText(this@MainActivity, "Определение...", Toast.LENGTH_SHORT).show()
                        }

                        is AppState.Success -> {
                            Toast.makeText(this@MainActivity, "Готово", Toast.LENGTH_SHORT).show()
                        }

                        is AppState.Error -> {
                            Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> {}
                    }
                }
            }

            launch {
                viewModel.location.collect { data ->

                        cnt++
                        Timber.tag("PLOCATION")
                            .d("${cnt}. Latitude: ${data?.latitude}, Longitude: ${data?.longitude}")
                    }
            }


        }
    }
}