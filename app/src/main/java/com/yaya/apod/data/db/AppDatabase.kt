package com.yaya.apod.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yaya.apod.data.db.dao.ApodDao
import com.yaya.apod.data.model.Apod

@Database(
    entities = [Apod::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun apodDao(): ApodDao
}