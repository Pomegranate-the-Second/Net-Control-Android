package com.rangebit.net_control_a.ui.measurement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.Utils
import com.google.gson.Gson
import com.rangebit.net_control_a.R
import com.rangebit.net_control_a.data.source.network.MeasurementManager
import com.rangebit.net_control_a.domain.model.MeasurementData
import com.rangebit.net_control_a.ui.main.AppIntent
import com.rangebit.net_control_a.ui.main.AppState
import com.rangebit.net_control_a.ui.main.MainActivity
import com.rangebit.net_control_a.ui.main.MainViewModel
import com.rangebit.net_control_a.ui.main.MainViewModelFactory
import com.rangebit.net_control_a.ui.settings.SettingsActivity
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.getValue

class MeasurementActivity : AppCompatActivity() {

    private lateinit var chart: LineChart
    private lateinit var lineData: LineData
    private lateinit var downloadSet: LineDataSet
    private lateinit var uploadSet: LineDataSet
    private lateinit var textViewD: TextView
    private lateinit var textViewU: TextView

    private val viewModel: MeasurementViewModel by viewModels {
        MeasurementViewModelFactory(MeasurementManager())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_measurement)

        val json = intent.getStringExtra("measurement_data_json")
        val measurementData = Gson().fromJson(json, MeasurementData::class.java)
        measurementData?.let {
            Timber.tag("MEASUREMENT").d("Received data: $it")
        }

        textViewD = findViewById<TextView>(R.id.speedTrackerDownlink)
        textViewU = findViewById<TextView>(R.id.speedTrackerUplink)

        initChart()
        observeViewModel()

        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        viewModel.handleIntent(AppIntent.StartMeasurement, this)

    }

    private fun observeViewModel() {

        lifecycleScope.launchWhenStarted {

            launch {
                viewModel.state.collect { state ->
                    when (state) {

                        is AppState.Idle -> {}

                        is AppState.Measuring -> {
                            Toast.makeText(this@MeasurementActivity, "Измерение...", Toast.LENGTH_SHORT).show()
                        }

                        is AppState.Success -> {
                            Toast.makeText(this@MeasurementActivity, "Готово", Toast.LENGTH_SHORT).show()
                        }

                        is AppState.Error -> {
                            Toast.makeText(this@MeasurementActivity, state.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> {}
                    }
                }
            }

            launch {
                viewModel.downloadSpeed.collect { speed ->
                    addDownloadPoint(speed)
                }
            }

            launch {
                viewModel.avgDownloadSpeed.collect { speed ->
                    if (speed == 0.0) {
                        textViewD.text = "Скорость downlink: ... Mbps"
                    } else {
                        textViewD.text = "Скорость downlink: $speed Mbps"
                    }
                }
            }

            launch {
                viewModel.uploadSpeed.collect { speed ->
                    addUploadPoint(speed)
                }
            }

            launch {
                viewModel.avgUploadSpeed.collect { speed ->
                    if (speed == 0.0) {
                        textViewU.text = "Скорость uplink: ... Mbps"
                    } else {
                        textViewU.text = "Скорость uplink: $speed Mbps"
                    }
                }
            }
        }
    }

    private fun addDownloadPoint(speed: Double) {
        lineData.addEntry(
            Entry(downloadSet.entryCount.toFloat(), speed.toFloat()),
            0
        )
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun addUploadPoint(speed: Double) {
        lineData.addEntry(
            Entry(uploadSet.entryCount.toFloat(), speed.toFloat()),
            1
        )
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun initChart() {

        Utils.init(this)
        chart = findViewById(R.id.speedChart)

        downloadSet = LineDataSet(mutableListOf(), "Download Mbps").apply {
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(false)
            color = Color.GREEN
            setCircleColor(Color.BLACK)  // Точки черные
            setCircleRadius(4f)
        }

        uploadSet = LineDataSet(mutableListOf(), "Upload Mbps").apply {
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(false)
            color = Color.BLUE
            setCircleColor(Color.BLACK)  // Точки черные
            setCircleRadius(4f)
        }

        lineData = LineData(downloadSet, uploadSet)

        chart.data = lineData
        chart.description.isEnabled = false
        chart.invalidate()
    }
}