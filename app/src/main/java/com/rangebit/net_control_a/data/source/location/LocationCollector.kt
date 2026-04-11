package com.rangebit.net_control_a.data.source.location

import android.content.Context
import com.google.android.gms.location.LocationServices
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import timber.log.Timber

object LocationCollector {

    interface LocationCallback {
        fun onResult(location: Location)
        fun onError(error: String)
    }

    fun collect(
        context: Context,
        callback: LocationCallback
    ) {

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    Timber.tag("COLLECT").d("Search me 1!")
                    callback.onResult(location)
                } else {
                    Timber.tag("COLLECT").d("Search me 2!")
                    callback.onError("Location is null")
                }
            }
            .addOnFailureListener { e ->
                Timber.tag("COLLECT").d("Search me 3!")
                callback.onError(e.message ?: "Unknown error")
            }
    }

    fun multiCollect(
        context: Context,
        callback: LocationCallback
    ) {

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L // интервал 3 секунды
        ).build()

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    Timber.tag("COLLECT").d(
                        "New location: ${location.latitude}, ${location.longitude}"
                    )
                    callback.onResult(location)
                }
            }
        }

    }
}