package com.rangebit.net_control_a.domain.repository

import com.rangebit.net_control_a.domain.event.MeasurementEvent
import kotlinx.coroutines.flow.Flow

interface MeasurementRepository {
    fun startMeasurement(): Flow<MeasurementEvent>
}