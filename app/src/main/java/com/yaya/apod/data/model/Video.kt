package com.yaya.apod.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class Video(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "date")
    val date: String?,

    @ColumnInfo(name = "explanation")
    val explanation: String?,

    @ColumnInfo(name = "media_type")
    val mediaType: String?,

    @ColumnInfo(name = "service_version")
    val serviceVersion: String?,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "url")
    val url: String?
)