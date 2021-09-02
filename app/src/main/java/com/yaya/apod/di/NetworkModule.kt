package com.yaya.apod.di

import com.yaya.apod.api.ApodApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @ApiNormal
    @Provides
    @Singleton
    fun provideApodApi(): ApodApi {
        return ApodApi.create()
    }

    @ApiWithLiveDataAdapter
    @Provides
    @Singleton
    fun provideApodApiWithLiveDataAdapter(): ApodApi {
        return ApodApi.createWithLiveDataAdapter()
    }

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ApiWithLiveDataAdapter

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ApiNormal
}