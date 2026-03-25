package com.rangebit.net_control_a.domain.event

import com.rangebit.net_control_a.domain.model.MeasurementData

sealed class MeasurementEvent {
    data class DownloadProgress(val speed: Double) : MeasurementEvent()
    data class DownloadCompleted(val speed: Double) : MeasurementEvent()
    data class UploadProgress(val speed: Double) : MeasurementEvent()
    data class UploadCompleted(val speed: Double) : MeasurementEvent()
    data class Completed(val data: MeasurementData) : MeasurementEvent()
    data class Error(val message: String) : MeasurementEvent()
}