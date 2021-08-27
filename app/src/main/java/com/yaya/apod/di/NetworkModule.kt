package com.yaya.apod.di

import com.yaya.apod.api.ApodService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideApodService(): ApodService {
        return ApodService.create()
    }
}