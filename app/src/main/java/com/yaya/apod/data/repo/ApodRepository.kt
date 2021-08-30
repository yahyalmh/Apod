package com.yaya.apod.data.repo

import androidx.lifecycle.LiveData
import com.yaya.apod.AppExecutors
import com.yaya.apod.api.ApiResponse
import com.yaya.apod.api.ApodService
import com.yaya.apod.data.db.AppDatabase
import com.yaya.apod.data.db.dao.ApodDao
import com.yaya.apod.data.model.Apod
import com.yaya.apod.util.Util
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApodRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val apodService: ApodService,
    private val db: AppDatabase,
    private val apodDao: ApodDao
) {

    suspend fun updateApod(apod: Apod) {
        apodDao.update(apod)
    }

    fun getTodayContent(): LiveData<Resource<Apod>> {
        return object : NetworkBoundResource<Apod, Apod>(appExecutors) {
            override fun saveCallResult(item: Apod) {
                apodDao.insert(item)
            }

            override fun shouldFetch(data: Apod?): Boolean {
                return data == null
            }

            override fun loadFromDb(): LiveData<Apod> {
                return apodDao.getByDate(Util.getTodayDate())
            }

            override fun createCall(): LiveData<ApiResponse<Apod>> {
                return apodService.getTodayContent()
            }
        }.asLiveData()
    }


    fun getContents(endDate: String, count: Int): LiveData<Resource<MutableList<Apod>>> {
        return object : NetworkBoundResource<MutableList<Apod>, Array<Apod>>(appExecutors) {
            override fun saveCallResult(item: Array<Apod>) {
                apodDao.insertAll(*item)
            }

            override fun shouldFetch(data: MutableList<Apod>?): Boolean {
                return data == null || data.size < count || data[0].date != Util.getTodayDate()
            }

            override fun loadFromDb(): LiveData<MutableList<Apod>> {
                return apodDao.getByDateCount(endDate, count)
            }

            override fun createCall(): LiveData<ApiResponse<Array<Apod>>> {
                val startDate = Util.getDateBeforeToday(daysCountBeforeToday = count)
                return apodService.getContents(startDate = startDate)
            }
        }.asLiveData()
    }
}