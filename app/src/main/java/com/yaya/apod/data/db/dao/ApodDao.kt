package com.yaya.apod.data.db.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.yaya.apod.data.model.Apod

@Dao
interface ApodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArray(vararg apods: Apod)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(apods: List<Apod>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(apod: Apod)

    @Update
    suspend fun update(apod: Apod)

    @Delete(entity = Apod::class)
    suspend fun delete(apod: Apod)

    @Query("DELETE FROM apod where date>=:startDate and date<=:endDate")
    fun deleteByDate(startDate: String, endDate: String)

    @Query("SELECT * FROM apod order by date desc")
    fun getAll(): PagingSource<Int, Apod>

    @Query("SELECT * FROM apod where favorite=1 order by date desc")
    fun getFavorite(): LiveData<MutableList<Apod>>

    @Query("SELECT * FROM apod where id==:id")
    fun getById(id: Int): LiveData<Apod>

    @Query("SELECT * FROM apod where date>=:start and date<=:end order by date")
    fun getByDatePeriod(start: String, end: String): PagingSource<Int, Apod>

    @Query("SELECT * FROM apod where date<=:start order by date desc limit :count")
    fun getByDateCount(start: String, count: Int): LiveData<MutableList<Apod>>

    @Query("SELECT * FROM apod where date==:date")
    fun getByDate(date: String): LiveData<Apod>

    @Query("SELECT date FROM apod order by date asc limit 1")
    fun getLastDate(): String?

    @Query("SELECT date FROM apod order by date desc limit 1")
    fun getTopDate(): String?

    @Query("SELECT count(*) FROM apod")
    fun getCount(): Int

    @Query("REPLACE INTO apod (copyright, date, explanation, url, hdurl, media_type, title, service_version) Values (:copyright, :date, :explanation, :url, :hdurl, :media_type, :title, :service_version)")
    fun replace(
        copyright: String?,
        date: String,
        explanation: String,
        url: String,
        hdurl: String?,
        media_type: String,
        title: String,
        service_version: String?
    )

}