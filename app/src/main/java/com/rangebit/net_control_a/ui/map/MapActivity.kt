package com.rangebit.net_control_a.ui.map

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.rangebit.net_control_a.R
import com.rangebit.net_control_a.data.source.location.LocationCollector
import com.rangebit.net_control_a.data.source.location.LocationManager
import com.rangebit.net_control_a.domain.model.MeasurementData
import com.rangebit.net_control_a.ui.main.AppIntent
import com.rangebit.net_control_a.ui.main.MainViewModel
import com.rangebit.net_control_a.ui.main.MainViewModelFactory
import com.rangebit.net_control_a.ui.measurement.MeasurementActivity
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import timber.log.Timber
import kotlin.getValue

class MapActivity : AppCompatActivity() {

    private val viewModel: MapViewModel by viewModels {
        MapViewModelFactory(LocationManager())
    }

    private lateinit var map: MapView
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var callback: LocationCollector.LocationCallback

    private var cnt = 0

    private var lastMeasurementData: MeasurementData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map)


        map = findViewById(R.id.map)

        findViewById<ImageButton>(R.id.btnLocation).setOnClickListener {
            val intent = Intent(this, MeasurementActivity::class.java).apply {
                val gson = Gson()
                val json = gson.toJson(lastMeasurementData)
                putExtra("measurement_data_json", json)
            }
            startActivity(intent)
        }

        // osmdroid config
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        map.setMultiTouchControls(true)
        map.controller.setZoom(18.0)

        locationClient = LocationServices.getFusedLocationProviderClient(this)

        observeViewModel()

        viewModel.handleIntent(AppIntent.StartLocating, this)

    }

    private fun observeViewModel() {

        lifecycleScope.launchWhenStarted {

            launch {
            viewModel.location.collect { data ->
                lastMeasurementData = data
                cnt++
                Timber.tag("MAPMEASURE")
                    .d("${cnt}.Device State: deviceID=${data?.deviceID}, lat=${data?.latitude}, lon=${data?.longitude}, mnc=${data?.mnc}, cid=${data?.cid}, pci=${data?.pci}, upload=${data?.upload}, download=${data?.download}, rsrp=${data?.rsrp}, rssi=${data?.rssi}")

                val geoPoint = GeoPoint(data?.latitude ?: 0.0 , data?.longitude ?: 0.0)

                runOnUiThread {
                    map.controller.animateTo(geoPoint)
                }
            }
        }


        }
    }
}