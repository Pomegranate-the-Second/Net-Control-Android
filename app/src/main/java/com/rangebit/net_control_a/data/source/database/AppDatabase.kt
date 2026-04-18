package com.rangebit.net_control_a.data.source.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TowerEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun towerDao(): TowerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val assets = context.assets.list("")
            Log.d("ASSETS", assets?.joinToString() ?: "empty")
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_db"
                ).createFromAsset("database/towers.db").fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}