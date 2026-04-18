package com.rangebit.net_control_a.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rangebit.net_control_a.data.source.database.DatabaseManager
import com.rangebit.net_control_a.data.source.location.LocationManager
import com.rangebit.net_control_a.data.source.network.MeasurementManager
import com.rangebit.net_control_a.domain.event.LocationEvent
import com.rangebit.net_control_a.domain.event.MeasurementEvent
import com.rangebit.net_control_a.domain.model.MeasurementData
import com.rangebit.net_control_a.ui.main.AppIntent
import com.rangebit.net_control_a.ui.main.AppState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import timber.log.Timber

class MapViewModelFactory(
    private val locationManager: LocationManager,
    private val databaseManager: DatabaseManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(com.rangebit.net_control_a.ui.map.MapViewModel::class.java)) {
            return MapViewModel(locationManager, databaseManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MapViewModel(
    private val locationManager: LocationManager,
    private val databaseManager: DatabaseManager
) : ViewModel() {

    private val _location = MutableStateFlow<MeasurementData?>(null)
    val location: MutableStateFlow<MeasurementData?> = _location
    private val _state = MutableStateFlow<AppState>(AppState.Idle)
    val state: StateFlow<AppState> = _state
    private val _towers = MutableStateFlow<List<GeoPoint>>(emptyList())
    val towers: StateFlow<List<GeoPoint>> = _towers

    private val _lineToTower = MutableSharedFlow<Pair<GeoPoint, GeoPoint>>()
    val lineToTower = _lineToTower.asSharedFlow()

    fun loadTowers() {
        viewModelScope.launch {
            databaseManager.getAllTowers().collect {
                _towers.value = it
            }
        }
    }

    fun handleIntent(intent: AppIntent, context: Context) {
        when (intent) {
            is AppIntent.StartLocating -> startPeriodicLocationUpdates(context)
            is AppIntent.OpenMap -> {
                // навигация через Activity
            }

            is AppIntent.OpenSettings -> {}
            else -> {}
        }
    }

    private fun handleMeasurement(data: MeasurementData) {
        viewModelScope.launch {

            val userPoint = GeoPoint(data.latitude, data.longitude)

            /*
            databaseManager.getTowerByEnodeb(data.cid shr 8)
                .collect { towerPoint ->

                    towerPoint?.let {
                        _lineToTower.emit(userPoint to it)
                    }
                }

             */
            val towerPoint = databaseManager
                .getTowerByEnodeb(data.cid shr 8)
                .first()

            towerPoint?.let {
                _lineToTower.emit(userPoint to it)
            }
        }
    }

    fun startPeriodicLocationUpdates(context: Context) {
        viewModelScope.launch {

            _state.value = AppState.Locating

            locationManager.startLocationCollection(context)
                .catch { event ->
                    _state.value = AppState.Error(event.message ?: "Unknown error")
                }
                .collect { event ->
                    when (event) {
                        is LocationEvent.Completed -> {}
                        is LocationEvent.Result -> {
                            Timber.tag("VIEW").d("Search me V 1!")
                            _location.value = event.data
                            handleMeasurement(event.data)
                        }

                        is LocationEvent.Error -> {
                            _state.value = AppState.Error(event.message)
                        }
                    }
                }
        }
    }
}