package com.yaya.apod.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yaya.apod.data.model.Apod

@Dao
interface ApodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg apods: Apod)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(apod: Apod)

    @Update
    suspend fun update(apod: Apod)

    @Delete
    suspend fun delete(apod: Apod)

    @Query("SELECT * FROM apod")
    fun getAll(): LiveData<MutableList<Apod>>

    @Query("SELECT * FROM apod where id==:id")
    fun getById(id: Int): LiveData<Apod>

    @Query("SELECT * FROM apod where date>=:start and date<=:end order by date")
    fun getByDatePeriod(start: String, end: String): LiveData<MutableList<Apod>>

    @Query("SELECT * FROM apod where date<=:start order by date desc limit :count")
    fun getByDateCount(start: String, count: Int): LiveData<MutableList<Apod>>

    @Query("SELECT * FROM apod where date==:date")
    fun getByDate(date: String): LiveData<Apod>
}