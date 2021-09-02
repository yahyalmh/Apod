package com.yaya.apod.data.repo

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.airbnb.lottie.utils.Utils
import com.yaya.apod.api.ApodApi
import com.yaya.apod.data.db.AppDatabase
import com.yaya.apod.data.model.Apod
import com.yaya.apod.di.NetworkModule
import com.yaya.apod.util.DateUtil
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalPagingApi
@Singleton
class ApodRemoteMediator @Inject constructor(
    @NetworkModule.ApiNormal private val apodApi: ApodApi,
    private val db: AppDatabase
) : RemoteMediator<Int, Apod>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Apod>): MediatorResult {
        try {
            val endDate = when (loadType) {
                LoadType.REFRESH -> DateUtil.todayDate()
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    var lastDate = db.withTransaction {
                        db.apodDao().getLastDate()
                    }
                    if ( lastDate == null){
                        lastDate = DateUtil.todayDate()
                    }
                    if (lastDate != DateUtil.todayDate()){
                        lastDate = DateUtil.getDateBeforeDate(lastDate, 1)
                    }
                    lastDate
                }
            }

            val startDate = DateUtil.getDateBeforeDate(endDate, state.config.pageSize)
            val data = apodApi.getTop(startDate = startDate, endDate = endDate)

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    db.apodDao().deleteByDate(startDate=startDate, endDate = endDate)
                }

                db.apodDao().insertList(data)
            }
            return MediatorResult.Success(endOfPaginationReached = data.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }
}