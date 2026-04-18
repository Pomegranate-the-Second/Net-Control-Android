package com.rangebit.net_control_a.data.source.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "towers")
data class TowerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val enodeb: Int,
    val lat: Double,
    val lon: Double
)

/*
@Entity(tableName = "towers")
data class CountEntity(

    val lat: Double,
    val lon: Double,
    val cnt: Long
)

 */