package com.rangebit.net_control_a.domain.model

data class MeasurementData(
    var deviceID: String? = null,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var mnc: String? = null,
    var cid: Int = 0,
    var pci: Int = 0,
    var upload: Double = 0.0,
    var download: Double = 0.0,
    var rsrp: Int = 0,
    var rssi: Int = 0
)
