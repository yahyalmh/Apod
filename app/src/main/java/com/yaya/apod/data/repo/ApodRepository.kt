package com.yaya.apod.data.repo

import com.yaya.apod.api.ApodService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApodRepository @Inject constructor(private val apodService: ApodService) {

    fun getTodayContent() = apodService.getTodayContent()
}