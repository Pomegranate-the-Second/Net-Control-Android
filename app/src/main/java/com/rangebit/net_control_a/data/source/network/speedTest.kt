package com.rangebit.net_control_a.data.source.network

import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

data class SpeedSample(val timeMs: Long, val speedMbps: Double)

class SpeedTest {

    interface SpeedTestCallback {
        fun onProgress(currentSpeedMbps: Double)
        fun onComplete(avgSpeedMbps: Double, samples: List<SpeedSample>)
        fun onError(e: Exception)
    }

    companion object {

        fun startDownlink(
            urlStr: String,
            durationSeconds: Int,
            sampleIntervalMs: Int,
            callback: SpeedTestCallback
        ) {
            Thread {
                val samples = mutableListOf<SpeedSample>()
                try {
                    val url = URL(urlStr)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.useCaches = false
                    conn.connect()

                    val input: InputStream = conn.inputStream
                    val buffer = ByteArray(16 * 1024)

                    val startTime = System.currentTimeMillis()
                    var lastSampleTime = startTime
                    var bytesSinceLast = 0L
                    var totalBytes = 0L

                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        bytesSinceLast += read
                        totalBytes += read

                        val now = System.currentTimeMillis()
                        if (now - lastSampleTime >= sampleIntervalMs) {
                            val seconds = (now - lastSampleTime) / 1000.0
                            val speedMbps = (bytesSinceLast * 8.0) / (seconds * 1_000_000)
                            samples.add(SpeedSample(now - startTime, speedMbps))
                            callback.onProgress(speedMbps)
                            bytesSinceLast = 0
                            lastSampleTime = now
                        }

                        if (now - startTime >= durationSeconds * 1000L) break
                    }

                    input.close()

                    val totalSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                    val avgMbps = (totalBytes * 8.0) / (totalSeconds * 1_000_000)
                    callback.onComplete(avgMbps, samples)

                } catch (e: Exception) {
                    callback.onError(e)
                }
            }.start()
        }

        fun startUplink(
            urlStr: String,
            durationSeconds: Int,
            sampleIntervalMs: Int,
            callback: SpeedTestCallback
        ) {
            Thread {
                val samples = mutableListOf<SpeedSample>()
                try {
                    val url = URL(urlStr)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.setChunkedStreamingMode(0)

                    val output: OutputStream = conn.outputStream
                    val buffer = ByteArray(16 * 1024)

                    val startTime = System.currentTimeMillis()
                    var lastSampleTime = startTime
                    var bytesSinceLast = 0L
                    var totalBytes = 0L

                    while (true) {
                        output.write(buffer)
                        bytesSinceLast += buffer.size
                        totalBytes += buffer.size

                        val now = System.currentTimeMillis()
                        if (now - lastSampleTime >= sampleIntervalMs) {
                            val seconds = (now - lastSampleTime) / 1000.0
                            val speedMbps = (bytesSinceLast * 8.0) / (seconds * 1_000_000)
                            samples.add(SpeedSample(now - startTime, speedMbps))
                            callback.onProgress(speedMbps)
                            bytesSinceLast = 0
                            lastSampleTime = now
                        }

                        if (now - startTime >= durationSeconds * 1000L) break
                    }

                    output.flush()
                    output.close()
                    conn.responseCode // чтобы инициировать отправку

                    val totalSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                    val avgMbps = (totalBytes * 8.0) / (totalSeconds * 1_000_000)
                    callback.onComplete(avgMbps, samples)

                } catch (e: Exception) {
                    callback.onError(e)
                }
            }.start()
        }
    }
}