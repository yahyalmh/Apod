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
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread

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
        } catch (e: Exception) {
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
            val data = try {
                requestApodByDate(startDate, endDate)
            } catch (e: HttpException) {
                requestApodByDate(startDate, DateUtil.getDateBeforeDate(endDate, 1))
            }
            db.withTransaction {
                db.apodDao().insertList(data)
            }
        } else {
            val topDate = db.withTransaction {
                db.apodDao().getTopDate()
            }
            if (topDate != DateUtil.todayDate()) {
                val todayApod = requestTodayApod()
                val apodTmp = db.withTransaction {
                    db.apodDao().getByDate(todayApod.date)
                }
                if (apodTmp == null) {
                    db.withTransaction {
                        db.apodDao().insert(todayApod)
                    }
                }
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
        while (true) {
            try {
                return apodApi.getContentByDatePeriod(startDate = startDate, endDate = endDate)
            } catch (e: SocketTimeoutException) {
                throw e
            }
        }
    }

    private suspend fun requestTodayApod(): Apod {
        if (!AndroidUtils.isInternetAvailable(context)) {
            throw InternetNotAvailableException(context.getString(R.string.InternetUnavailableToday))
        }
        while (true) {
            try {
                return apodApi.getTodayContent()
            } catch (e: SocketTimeoutException) {
                throw e
            }
}
    }

    class InternetNotAvailableException(override val message: String) : Exception()
}

