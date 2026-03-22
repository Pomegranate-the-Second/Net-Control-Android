package com.rangebit.net_control_a.data.source.network

import android.app.Activity
import android.content.Context
import android.location.Location
import com.rangebit.net_control_a.data.source.location.LocationCollector
import com.rangebit.net_control_a.data.source.location.LocationHelper
import com.rangebit.net_control_a.domain.event.MeasurementEvent
import com.rangebit.net_control_a.domain.model.MeasurementData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class MeasurementManager {

    fun startMeasurement(context: Context): Flow<MeasurementEvent> = callbackFlow {

        val data = MeasurementData()

        LocationHelper.requestLocationPermission(context as Activity) {

            LocationCollector.collect(context, object : LocationCollector.LocationCallback {
                override fun onResult(location: Location) {
                    data.latitude = location.latitude
                    data.longitude = location.longitude
                }

                override fun onError(error: String) {
                    trySend(MeasurementEvent.Error(error))
                }
            })

            var lastDownloadSpeed = 0.0

            // 🔽 DOWNLOAD
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
                        // можно игнорировать
                    }

                    override fun onError(e: Exception) {
                        trySend(MeasurementEvent.Error(e.message ?: "Download error"))
                    }
                }
            )

            // 🔥 Таймер → запуск upload
            launch {
                kotlinx.coroutines.delay(10_000)

                // фиксируем download вручную
                data.download = lastDownloadSpeed

                var lastUploadSpeed = 0.0

                // 🔼 UPLOAD
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
                            trySend(MeasurementEvent.Completed(data))
                            close()
                        }

                        override fun onError(e: Exception) {
                            trySend(MeasurementEvent.Error(e.message ?: "Upload error"))
                        }
                    }
                )
            }
        }

        awaitClose { }
    }
}