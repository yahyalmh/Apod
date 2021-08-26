package com.yaya.apod.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yaya.apod.data.dao.ImageDao
import com.yaya.apod.data.dao.VideoDao
import com.yaya.apod.data.model.Image
import com.yaya.apod.data.model.Video

@Database(entities = [Image::class, Video::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
    abstract fun videoDao(): VideoDao
}