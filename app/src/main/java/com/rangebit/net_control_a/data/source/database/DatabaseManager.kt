package com.rangebit.net_control_a.data.source.database

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.osmdroid.util.GeoPoint

class DatabaseManager(context: Context) {

    private val dao = AppDatabase.getDatabase(context).towerDao()

    fun getAllTowers(): Flow<List<GeoPoint>> {
        return dao.getAllTowers()
            .map { list ->
                list.map { GeoPoint(it.lat, it.lon) }
            }
    }

    fun getTowerByEnodeb(enodeb: Int): Flow<GeoPoint?> {
        return dao.getTowerByEnodeb(enodeb)
            .map { entity ->
                entity?.let { GeoPoint(it.lat, it.lon) }
            }
    }
}