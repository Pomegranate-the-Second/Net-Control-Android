package com.rangebit.net_control_a.ui.main

import com.rangebit.net_control_a.domain.model.MeasurementData

sealed class AppState {
    object Idle : AppState()
    object Loading : AppState()
    object Measuring : AppState()

    data class Success(val data: MeasurementData) : AppState()
    data class Error(val message: String) : AppState()
}