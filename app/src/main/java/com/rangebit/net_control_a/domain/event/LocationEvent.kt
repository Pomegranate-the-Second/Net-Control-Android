package com.rangebit.net_control_a.domain.event

import com.rangebit.net_control_a.domain.model.MeasurementData

sealed class LocationEvent {

    data class Completed(val data: MeasurementData) : LocationEvent()
    data class Result(val data: MeasurementData) : LocationEvent()
    data class Error(val message: String) : LocationEvent()

}