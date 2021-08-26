package com.yaya.apod.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class Image(
    @PrimaryKey val id: Int,

    @ColumnInfo(name = "copyright")
    val copyright: String?,

    @ColumnInfo(name = "date")
    val date: String?,

    @ColumnInfo(name = "explanation")
    val explanation: String?,

    @ColumnInfo(name = "hdurl")
    val hdUrl: String?,

    @ColumnInfo(name = "media_type")
    val mediaType: String?,

    @ColumnInfo(name = "service_version")
    val serviceVersion: String?,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "url")
    val url: String?

)