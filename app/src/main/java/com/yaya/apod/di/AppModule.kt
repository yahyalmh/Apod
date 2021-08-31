package com.yaya.apod.di

import android.content.Context
import androidx.room.Room
import com.yaya.apod.data.db.AppDatabase
import com.yaya.apod.data.db.migration.ALL_MIGRATION
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
        val dbName = "Apod.db"
        return Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(*ALL_MIGRATION)
            .build()
    }

    @Singleton
    @Provides
    fun provideApodDao(db: AppDatabase) = db.apodDao()

}