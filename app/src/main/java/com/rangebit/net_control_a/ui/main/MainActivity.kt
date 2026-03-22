package com.rangebit.net_control_a.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
import com.rangebit.net_control_a.data.source.network.MeasurementManager
import com.rangebit.net_control_a.ui.measurement.MeasurementActivity
import kotlinx.coroutines.launch
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(MeasurementManager())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        //initChart()



        findViewById<Button>(R.id.btnStartTest).setOnClickListener {
            val intent = Intent(this, MeasurementActivity::class.java)
            startActivity(intent)
        }

        observeViewModel()
    }

    private fun observeViewModel() {

        lifecycleScope.launchWhenStarted {

            launch {
                viewModel.state.collect { state ->
                    when (state) {

                        is AppState.Idle -> {}

                        is AppState.Measuring -> {
                            Toast.makeText(this@MainActivity, "Измерение...", Toast.LENGTH_SHORT).show()
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

            /*
            launch {
                viewModel.downloadSpeed.collect { speed ->
                    addDownloadPoint(speed)
                }
            }

             */

            /*
            launch {
                viewModel.uploadSpeed.collect { speed ->
                    addUploadPoint(speed)
                }
            }

             */
        }
    }

/*
    private fun addDownloadPoint(speed: Double) {
        lineData.addEntry(
            Entry(downloadSet.entryCount.toFloat(), speed.toFloat()),
            0
        )
        //chart.notifyDataSetChanged()
        //chart.invalidate()

    }

 */
/*
    private fun addUploadPoint(speed: Double) {
        lineData.addEntry(
            Entry(uploadSet.entryCount.toFloat(), speed.toFloat()),
            1
        )
        //chart.notifyDataSetChanged()
        //chart.invalidate()

    }
    */
/*

    private fun initChart() {

        Utils.init(this)

        speedChart = findViewById(R.id.speedChart)

        chart = findViewById(R.id.speedChart)

        downloadSet = LineDataSet(mutableListOf(), "Download Mbps").apply {
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
        }

        uploadSet = LineDataSet(mutableListOf(), "Upload Mbps").apply {
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
        }

        lineData = LineData(downloadSet, uploadSet)
        speedChart?.data = lineData

        speedChart?.description?.isEnabled = false

        speedChart?.invalidate()

        chart?.data = lineData

        chart?.description?.isEnabled = false

        chart?.invalidate()
    }
*/
}