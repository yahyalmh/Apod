package com.yaya.apod.di

import android.content.Context
import androidx.room.Room
import com.yaya.apod.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun getDatabase(@ApplicationContext context: Context): AppDatabase {
        val dbName = "app_database"
        return Room.databaseBuilder(context, AppDatabase::class.java, dbName).build()
    }

}