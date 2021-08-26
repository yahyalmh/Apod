package com.yaya.apod.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.yaya.apod.data.model.Video
import java.util.*

@Dao
interface VideoDao {
    @Insert
    fun insertAll(vararg videos: Video)

    @Delete
    fun delete(video: Video)

    @Query("SELECT * FROM videos")
    fun getAll(): List<Video>

    @Query("SELECT * FROM videos where id==:id")
    fun getById(id: Int): List<Video>

    @Query("SELECT * FROM videos where date>=:start and date<=:end")
    fun getByDate(start: Date, end: Date): List<Video>
}