package com.rangebit.net_control_a.data.repository

import com.rangebit.net_control_a.data.source.location.LocationCollector
import com.rangebit.net_control_a.data.source.network.ApiClient
import com.rangebit.net_control_a.domain.event.MeasurementEvent
import com.rangebit.net_control_a.domain.model.MeasurementData
import com.rangebit.net_control_a.domain.repository.MeasurementRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/*
class MeasurementRepositoryImpl(
    private val apiClient: ApiClient
) : MeasurementRepository {

    override suspend fun sendMeasurement(data: MeasurementData) {
        apiClient.sendMeasurement(data)
    }
}

 */