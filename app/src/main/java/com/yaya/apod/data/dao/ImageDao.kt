package com.yaya.apod.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.yaya.apod.data.model.Image
import java.util.*

@Dao
interface ImageDao {
    @Insert
    fun insertAll(vararg images: Image)

    @Delete
    fun delete(image: Image)

    @Query("SELECT * FROM images")
    fun getAll(): List<Image>

    @Query("SELECT * FROM images where id==:id")
    fun getById(id: Int): List<Image>

    @Query("SELECT * FROM images where date>=:start and date<=:end")
    fun getByDate(start: Date, end: Date): List<Image>
}