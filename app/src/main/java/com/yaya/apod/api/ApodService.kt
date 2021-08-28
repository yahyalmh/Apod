package com.yaya.apod.api

import androidx.lifecycle.LiveData
import com.yaya.apod.BuildConfig
import com.yaya.apod.api.calladapter.LiveDataCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApodService {

    @GET("apod")
    fun getTodayContent(@Query("api_key") apiKey: String = BuildConfig.APOD_API_KEY):
            LiveData<ApiResponse<ApodResponse>>

    companion object {
        private const val BASE_URL = "https://api.nasa.gov/planetary/"

        fun create(): ApodService {
            val logger = HttpLoggingInterceptor().apply { level = Level.BASIC }

            val client = OkHttpClient.Builder().addInterceptor(logger).build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApodService::class.java)
        }
    }
}