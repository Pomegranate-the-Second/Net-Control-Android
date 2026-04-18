package com.rangebit.net_control_a.data.source.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.rangebit.net_control_a.domain.event.LocationEvent
import com.rangebit.net_control_a.domain.event.MeasurementEvent
import com.rangebit.net_control_a.domain.model.MeasurementData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import android.provider.Settings

class LocationManager {

    fun oldstartLocationCollection(context: Context): Flow<LocationEvent> = callbackFlow {

        LocationHelper.requestLocationPermission(context as Activity) {
            LocationCollector.multiCollect(context, object : LocationCollector.LocationCallback {
                override fun onResult(location: Location) {
                    val data = MeasurementData().apply {
                        latitude = location.latitude
                        longitude = location.longitude
                    }
                    Timber.tag("MANAGE").d("Search me M 1!")
                    trySend(LocationEvent.Result(data))
                }

                override fun onError(error: String) {
                    trySend(LocationEvent.Error(error))
                }
            })
        }

        awaitClose {  }
    }

    fun startLocationCollection(context: Context): Flow<LocationEvent> = callbackFlow {

        val activity = context as? Activity
            ?: run {
                trySend(LocationEvent.Error("Context is not Activity"))
                close()
                return@callbackFlow
            }

        var fusedLocationClient: FusedLocationProviderClient? = null
        var callback: com.google.android.gms.location.LocationCallback? = null

        var lastLocation: Location? = null
        var lastUpdateTime: Long = 0
        val MIN_DISTANCE_METERS = 10f
        val MAX_TIME_WITHOUT_UPDATE_MS = 30000L

        LocationHelper.requestLocationPermission(activity) {

            fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context)

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000L
            ).build()

            callback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    for (location in result.locations) {

                        val prev = lastLocation

                        val currentTime = System.currentTimeMillis()
                        if (prev != null && location.distanceTo(prev) < MIN_DISTANCE_METERS) {
                            if (currentTime - lastUpdateTime < MAX_TIME_WITHOUT_UPDATE_MS) {
                                continue
                            }
                        }
                        lastUpdateTime = currentTime

                        lastLocation = location

                        val data = MeasurementData().apply {
                            latitude = location.latitude
                            longitude = location.longitude

                            deviceID = Settings.Secure.getString(
                                activity.contentResolver,
                                Settings.Secure.ANDROID_ID
                            )
                        }

                        /*
                        val data = MeasurementData().apply {
                            latitude = location.latitude
                            longitude = location.longitude
                        }

                         */

                        CellTowerHelper.fillCellData(activity, data)

                        trySend(LocationEvent.Result(data))
                    }
                }
            }

            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                callback!!,
                Looper.getMainLooper()
            )
        }

        awaitClose {
            callback?.let {
                fusedLocationClient?.removeLocationUpdates(it)
            }
        }
    }
}

object CellTowerHelper {

    fun fillCellData(activity: Activity, data: MeasurementData) {

        val tm = activity.getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val cellInfos = try {
            tm.allCellInfo
        } catch (e: SecurityException) {
            Timber.tag("CELL").e(e, "Permission revoked")
            return
        } ?: return

        val mainCell = cellInfos.firstOrNull { it.isRegistered } ?: return

        when (mainCell) {

            is CellInfoLte -> {
                val identity = mainCell.cellIdentity
                val signal = mainCell.cellSignalStrength

                data.apply {
                    mnc = identity.mncString
                    cid = identity.ci
                    pci = identity.pci
                    rsrp = signal.rsrp
                    rssi = signal.rssi
                }
            }

            is CellInfoWcdma -> {
                val identity = mainCell.cellIdentity
                val signal = mainCell.cellSignalStrength

                data.apply {
                    mnc = identity.mncString
                    cid = identity.cid
                    pci = -1
                    rsrp = -1
                    rssi = signal.dbm
                }
            }

            is CellInfoGsm -> {
                val identity = mainCell.cellIdentity
                val signal = mainCell.cellSignalStrength

                data.apply {
                    mnc = identity.mncString
                    cid = identity.cid
                    pci = -1
                    rsrp = -1
                    rssi = signal.dbm
                }
            }
        }
    }
}