package com.rangebit.net_control_a.ui.main

sealed class AppIntent {
    object StartMeasurement : AppIntent()
    object OpenMap : AppIntent()
    object OpenSettings : AppIntent()
    object StartLocating : AppIntent()
}