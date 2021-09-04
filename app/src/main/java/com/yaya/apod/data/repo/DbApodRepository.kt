package com.yaya.apod.data.repo

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.yaya.apod.api.ApodApi
import com.yaya.apod.data.db.AppDatabase
import com.yaya.apod.data.db.dao.ApodDao
import com.yaya.apod.data.model.Apod
import com.yaya.apod.di.NetworkModule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbApodRepository @Inject constructor(
    @NetworkModule.ApiNormal private val apodApi: ApodApi,
    private val apodDao: ApodDao
) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 8
    }

    @ExperimentalPagingApi
    @Inject
    lateinit var remoteMediator: ApodRemoteMediator

    @OptIn(ExperimentalPagingApi::class)
    fun fetchApodPage(): Flow<PagingData<Apod>> {
        return Pager(
            config = PagingConfig(pageSize = DEFAULT_PAGE_SIZE),
            remoteMediator = remoteMediator
        ) {
            apodDao.getAll()
        }.flow
    }

}
