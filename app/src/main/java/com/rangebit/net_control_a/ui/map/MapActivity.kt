package com.rangebit.net_control_a.ui.map

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.rangebit.net_control_a.R
import com.rangebit.net_control_a.data.source.database.DatabaseManager
import com.rangebit.net_control_a.data.source.location.LocationCollector
import com.rangebit.net_control_a.data.source.location.LocationManager
import com.rangebit.net_control_a.domain.model.MeasurementData
import com.rangebit.net_control_a.ui.main.AppIntent
import com.rangebit.net_control_a.ui.main.MainViewModel
import com.rangebit.net_control_a.ui.main.MainViewModelFactory
import com.rangebit.net_control_a.ui.measurement.MeasurementActivity
import kotlinx.coroutines.launch
import okhttp3.internal.platform.PlatformRegistry
import okhttp3.internal.platform.PlatformRegistry.applicationContext
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polyline
import timber.log.Timber
import kotlin.getValue

class MapActivity : AppCompatActivity() {

    private val viewModel: MapViewModel by viewModels {
        MapViewModelFactory(
            LocationManager(),
            DatabaseManager(applicationContext)
        )
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

        viewModel.loadTowers()

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
                Timber.tag("MAPMEASURE").d("EnodeB ${(data?.cid ?: 0) shr 8}")
                val geoPoint = GeoPoint(data?.latitude ?: 0.0 , data?.longitude ?: 0.0)

                runOnUiThread {
                    map.controller.animateTo(geoPoint)
                }
            }
        }
            launch {
                viewModel.towers.collect { towers ->
                    val twr: List<GeoPoint> = listOf(GeoPoint(40.7128, -74.0060))
                    drawTowers(towers)
                }
            }

            launch {
                viewModel.lineToTower.collect { (from, to) ->
                    drawLine(from, to)
                }
            }


        }
    }

    private fun drawTowers(points: List<GeoPoint>) {
        map.overlays.clear()

        val icon = ContextCompat.getDrawable(this, R.drawable.tower_32)
        points.forEach { point ->
            val marker = Marker(map).apply {
                position = point
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                this.icon = icon
            }
            map.overlays.add(marker)
        }

        map.invalidate()
    }

    private fun drawLine(from: GeoPoint, to: GeoPoint) {
        map.overlays.removeAll { it is Polyline }
        val line = Polyline().apply {
            setPoints(listOf(from, to))
            outlinePaint.color = Color.BLUE
            outlinePaint.pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0f)
        }

        map.overlays.add(line)
        map.invalidate()
    }
}