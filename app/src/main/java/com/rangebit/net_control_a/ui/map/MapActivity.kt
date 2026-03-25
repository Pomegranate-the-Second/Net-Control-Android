package com.rangebit.net_control_a.ui.map

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.rangebit.net_control_a.R
import com.rangebit.net_control_a.data.source.location.LocationCollector
import com.rangebit.net_control_a.ui.main.AppIntent
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class MapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var callback: LocationCollector.LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map)


        map = findViewById(R.id.map)

        // osmdroid config
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        map.setMultiTouchControls(true)
        map.controller.setZoom(18.0)

        locationClient = LocationServices.getFusedLocationProviderClient(this)

        //startLocationUpdates()

    }

    /*
    private fun startLocationUpdates() {

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000 // каждые 3 секунды
        ).build()

        val callback = object : LocationCollector.LocationCallback() {
            override fun onLocationResult(result: LocationResult) {

                val location = result.lastLocation ?: return

                val geoPoint = GeoPoint(location.latitude, location.longitude)

                runOnUiThread {
                    map.controller.animateTo(geoPoint)
                }
            }
        }

        locationClient.requestLocationUpdates(
            request,
            callback,
            mainLooper
        )
    }

    override fun onPause() {
        super.onPause()
        locationClient.removeLocationUpdates(callback)
        map.onPause()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

     */
}