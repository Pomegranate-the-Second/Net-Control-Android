package com.rangebit.net_control_a.data.source.network

import android.app.Activity
import android.content.Context
import android.location.Location
import android.provider.Settings
import com.rangebit.net_control_a.data.source.cellular.CellTowerCollector
import com.rangebit.net_control_a.data.source.location.LocationCollector
import com.rangebit.net_control_a.data.source.location.LocationHelper
import com.rangebit.net_control_a.domain.event.MeasurementEvent
import com.rangebit.net_control_a.domain.model.MeasurementData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MeasurementManager {

    fun startMeasurement(context: Context): Flow<MeasurementEvent> = callbackFlow {

        val data = MeasurementData()

        LocationHelper.requestLocationPermission(context as Activity) {

            data.deviceID = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            LocationCollector.collect(context, object : LocationCollector.LocationCallback {
                override fun onResult(location: Location) {
                    data.latitude = location.latitude
                    data.longitude = location.longitude
                }

                override fun onError(error: String) {
                    trySend(MeasurementEvent.Error(error))
                }
            })

            val towers = CellTowerCollector.collect(context)
            towers.firstOrNull { it.isRegistered }?.let {
                data.mnc = it.mnc
                data.cid = it.cid
                data.pci = it.pci
                data.rsrp = it.rsrp
                data.rssi = it.rssi
            }

            var lastDownloadSpeed = 0.0

            SpeedTest.startDownlink(
                "https://rangebit.top/b0e349b6-aaa3-4341-b7f7-39102f6243a1",
                10,
                500,
                object : SpeedTest.SpeedTestCallback {

                    override fun onProgress(speed: Double) {
                        lastDownloadSpeed = speed
                        trySend(MeasurementEvent.DownloadProgress(speed))
                    }

                    override fun onComplete(avg: Double, samples: List<SpeedSample>) {
                        data.download = lastDownloadSpeed
                        trySend(MeasurementEvent.DownloadCompleted(lastDownloadSpeed))

                        var lastUploadSpeed = 0.0

                        SpeedTest.startUplink(
                            "https://rangebit.top/c8686ff0-ae42-4883-9216-b56a1a70d555",
                            10,
                            500,
                            object : SpeedTest.SpeedTestCallback {

                                override fun onProgress(speed: Double) {
                                    lastUploadSpeed = speed
                                    trySend(MeasurementEvent.UploadProgress(speed))
                                }

                                override fun onComplete(avg: Double, samples: List<SpeedSample>) {
                                    data.upload = avg.takeIf { it > 0 } ?: lastUploadSpeed
                                    trySend(MeasurementEvent.UploadCompleted(lastUploadSpeed))
                                    trySend(MeasurementEvent.Completed(data))
                                    ApiClient.sendMeasurement(data)
                                    close()
                                }

                                override fun onError(e: Exception) {
                                    trySend(MeasurementEvent.Error(e.message ?: "Upload error"))
                                }
                            }
                        )
                    }

                    override fun onError(e: Exception) {
                        trySend(MeasurementEvent.Error(e.message ?: "Download error"))
                    }
                }
            )

        }

        awaitClose {  }
    }
}