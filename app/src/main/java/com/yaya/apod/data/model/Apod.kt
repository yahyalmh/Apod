package com.yaya.apod.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "apod")
data class Apod(
    @PrimaryKey(autoGenerate = true)
    @field:Expose
    val id: Int,

    @field:SerializedName("copyright")
    @field:Expose
    @ColumnInfo(name = "copyright")
    val copyright: String?,

    @field:SerializedName("date")
    @ColumnInfo(name = "date")
    val date: String,

    @field:SerializedName("explanation")
    @ColumnInfo(name = "explanation")
    val explanation: String,

    @field:SerializedName("hdurl")
    @field:Expose
    @ColumnInfo(name = "hdurl")
    val hdUrl: String?,

    @field:SerializedName("media_type")
    @ColumnInfo(name = "media_type")
    val mediaType: String,

    @field:SerializedName("service_version")
    @ColumnInfo(name = "service_version")
    val serviceVersion: String?,

    @field:SerializedName("title")
    @ColumnInfo(name = "title")
    val title: String,

    @field:SerializedName("url")
    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "favorite", defaultValue = "false")
    var favorite: Boolean = false
)