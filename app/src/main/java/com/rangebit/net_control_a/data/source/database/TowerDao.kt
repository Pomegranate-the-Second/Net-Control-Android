package com.rangebit.net_control_a.data.source.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TowerDao {

    @Query("SELECT * FROM towers")
    fun getAllTowers(): Flow<List<TowerEntity>>

    @Query("SELECT * FROM towers WHERE enodeb = :enodeb LIMIT 1")
    fun getTowerByEnodeb(enodeb: Int): Flow<TowerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(towers: List<TowerEntity>)
}