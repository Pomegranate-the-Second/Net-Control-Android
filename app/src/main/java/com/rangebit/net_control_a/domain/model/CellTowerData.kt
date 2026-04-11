package com.rangebit.net_control_a.domain.model

data class CellTowerData(
    var mnc: String? = null,
    var cid: Int = 0,
    var pci: Int = 0,
    var rsrp: Int = 0,
    var rssi: Int = 0,
    var isRegistered: Boolean = false
)