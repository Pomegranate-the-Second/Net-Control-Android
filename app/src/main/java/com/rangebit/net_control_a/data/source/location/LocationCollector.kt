package com.rangebit.net_control_a.data.source.location

import android.content.Context
import com.google.android.gms.location.LocationServices
import android.location.Location

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
                    callback.onResult(location)
                } else {
                    callback.onError("Location is null")
                }
            }
            .addOnFailureListener { e ->
                callback.onError(e.message ?: "Unknown error")
            }
    }
}