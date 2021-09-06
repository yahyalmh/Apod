package com.yaya.apod.data.repo

import android.content.Context
import android.widget.Toast
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.yaya.apod.R
import com.yaya.apod.api.ApodApi
import com.yaya.apod.data.db.AppDatabase
import com.yaya.apod.data.model.Apod
import com.yaya.apod.di.NetworkModule
import com.yaya.apod.util.AndroidUtils
import com.yaya.apod.util.DateUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalPagingApi
@Singleton
class ApodRemoteMediator @Inject constructor(
    @ApplicationContext private val context: Context,
    @NetworkModule.ApiNormal private val apodApi: ApodApi,
    private val db: AppDatabase
) : RemoteMediator<Int, Apod>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Apod>): MediatorResult {
        return try {
            when (loadType) {
                LoadType.PREPEND -> MediatorResult.Success(endOfPaginationReached = true)
                LoadType.REFRESH -> refresh(state)
                LoadType.APPEND -> append(state)
            }
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        } catch (e: InternetNotAvailableException) {
            MediatorResult.Error(e)
        }
    }

    /**
     * This method will get today Apod and add at the beginning of the list
     */
    private suspend fun refresh(state: PagingState<Int, Apod>): MediatorResult {
        val lastDate = db.withTransaction {
            db.apodDao().getLastDate()
        }
        val count = db.withTransaction {
            db.apodDao().getCount()
        }
        if (lastDate == null || count == 0) {
            val endDate = DateUtil.todayDate()
            val startDate = DateUtil.getDateBeforeDate(endDate, state.config.pageSize)
            val data = requestApodByDate(startDate, endDate)
            db.withTransaction {
                db.apodDao().insertList(data)
            }
        } else {
            val topDate = db.withTransaction {
                db.apodDao().getTopDate()
            }
            if (topDate != DateUtil.todayDate()) {
                val todayApod = requestTodayApod()
                db.withTransaction {
                    db.apodDao().insert(todayApod)
                }
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.Updated),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
        return MediatorResult.Success(endOfPaginationReached = false)

    }

    /**
     * This method try to get new data based on last date in db and add at the end of list
     */
    private suspend fun append(state: PagingState<Int, Apod>): MediatorResult {
        var lastDate = db.withTransaction {
            db.apodDao().getLastDate()
        }
        if (lastDate == null) {
            lastDate = DateUtil.todayDate()
        }
        if (lastDate != DateUtil.todayDate()) {
            lastDate = DateUtil.getDateBeforeDate(lastDate, 1)
        }
        val startDate = DateUtil.getDateBeforeDate(lastDate, state.config.pageSize)
        val data = requestApodByDate(startDate = startDate, endDate = lastDate)
        db.withTransaction { db.apodDao().insertList(data) }
        return MediatorResult.Success(endOfPaginationReached = false)
    }

    private suspend fun requestApodByDate(
        startDate: String,
        endDate: String
    ): List<Apod> {
        if (!AndroidUtils.isInternetAvailable(context)) {
            throw InternetNotAvailableException(context.getString(R.string.InternetUnavailable))
        }

        return apodApi.getContentByDatePeriod(startDate = startDate, endDate = endDate)
    }

    private suspend fun requestTodayApod(): Apod {
        if (!AndroidUtils.isInternetAvailable(context)) {
            throw InternetNotAvailableException(context.getString(R.string.InternetUnavailableToday))
        }
        return apodApi.getTodayContent()
    }

    class InternetNotAvailableException(override val message: String) : Exception()
}

