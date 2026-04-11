package com.rangebit.net_control_a.data.source.network

import com.rangebit.net_control_a.domain.model.MeasurementData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.Random

object ApiClient {

    private val client = OkHttpClient()
    private val random = Random()
    private const val OFFSET_RANGE = 0.009999

    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun addRandomOffset(coordinate: Double): Double {
        val offset = (random.nextDouble() * 2 - 1) * OFFSET_RANGE
        return kotlin.math.round((coordinate + offset) * 1_000_000.0) / 1_000_000.0
    }

    private fun getBaseStationId(cid: Int): Int {
        return cid shr 8
    }

    private fun getSectorId(cid: Int): Int {
        return cid and 255
    }

    fun sendMeasurement(data: MeasurementData) {

        try {
            val cid = data.cid

            val baseStation = getBaseStationId(cid)
            val sector = getSectorId(cid)

            val json = JSONObject().apply {
                put("android_id", data.deviceID)
                put("lat", addRandomOffset(data.latitude))
                put("lon", addRandomOffset(data.longitude))
                put("bs_num", baseStation)
                put("cell_num", sector)
                put("operator", data.mnc)
                put("upload", kotlin.math.round(data.upload * 100.0) / 100.0)
                put("download", kotlin.math.round(data.download * 100.0) / 100.0)
                put("rsrp", data.rsrp)
                put("rssi", data.rssi)
                put("android_id", data.deviceID)
            }

            Timber.tag("NETWORK").d("Request JSON: ${json.toString()}")

            val body = json.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("https://rangebit.top/measure/add")
                .put(body)
                .build()

            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    Timber.tag("NETWORK").e(e, "Ошибка отправки данных")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (it.isSuccessful) {
                            Timber.tag("NETWORK").d("Данные успешно отправлены")
                        } else {
                            Timber.tag("NETWORK").e(
                                "Ошибка сервера: code=${it.code}, message=${it.message}"
                            )
                        }
                    }
                }
            })

        } catch (e: Exception) {
            Timber.tag("NETWORK").e(e, "Ошибка при формировании json")
        }
    }
}