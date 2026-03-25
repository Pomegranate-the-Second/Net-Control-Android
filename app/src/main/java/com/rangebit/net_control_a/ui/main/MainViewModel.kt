package com.rangebit.net_control_a.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rangebit.net_control_a.data.source.network.MeasurementManager
import com.rangebit.net_control_a.domain.event.MeasurementEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModelFactory(
    private val measurementManager: MeasurementManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(measurementManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainViewModel(
    private val measurementManager: MeasurementManager
) : ViewModel() {

    private val _state = MutableStateFlow<AppState>(AppState.Idle)
    val state: StateFlow<AppState> = _state

    private val _downloadSpeed = MutableStateFlow(0.0)
    val downloadSpeed: StateFlow<Double> = _downloadSpeed

    private val _avgDownloadSpeed = MutableStateFlow(0.0)
    val avgDownloadSpeed: StateFlow<Double> = _avgDownloadSpeed

    private val _uploadSpeed = MutableStateFlow(0.0)
    val uploadSpeed: StateFlow<Double> = _uploadSpeed

    private val _avgUploadSpeed = MutableStateFlow(0.0)
    val avgUploadSpeed: StateFlow<Double> = _avgUploadSpeed

    fun handleIntent(intent: AppIntent, context: Context) {
        when (intent) {
            is AppIntent.StartMeasurement -> startMeasurement(context)
            is AppIntent.OpenMap -> {
                // навигация через Activity
            }
            is AppIntent.OpenSettings -> {}
        }
    }

    private fun startMeasurement(context: Context) {
        viewModelScope.launch {

            _state.value = AppState.Measuring

            measurementManager.startMeasurement(context)
                .collect { event ->

                    when (event) {

                        is MeasurementEvent.DownloadProgress -> {
                            _downloadSpeed.value = event.speed
                        }

                        is MeasurementEvent.DownloadCompleted -> {
                            _avgDownloadSpeed.value = event.speed
                        }

                        is MeasurementEvent.UploadProgress -> {
                            _uploadSpeed.value = event.speed
                        }

                        is MeasurementEvent.UploadCompleted -> {
                            _avgUploadSpeed.value = event.speed
                        }

                        is MeasurementEvent.Completed -> {
                            _state.value = AppState.Success(event.data)
                        }

                        is MeasurementEvent.Error -> {
                            _state.value = AppState.Error(event.message)
                        }
                    }
                }
        }
    }
}