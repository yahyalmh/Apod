package com.yaya.apod.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "apod")
data class Apod(
    @PrimaryKey(autoGenerate = true)
    @Expose
    val id: Int,

    @SerializedName("copyright")
    @Expose
    @ColumnInfo(name = "copyright")
    val copyright: String?,

    @SerializedName("date")
    @ColumnInfo(name = "date")
    val date: String,

    @SerializedName("explanation")
    @ColumnInfo(name = "explanation")
    val explanation: String,

    @SerializedName("hdurl")
    @field:Expose
    @ColumnInfo(name = "hdurl")
    val hdUrl: String?,

    @SerializedName("media_type")
    @ColumnInfo(name = "media_type")
    val mediaType: String,

    @SerializedName("service_version")
    @ColumnInfo(name = "service_version")
    val serviceVersion: String?,

    @SerializedName("title")
    @ColumnInfo(name = "title")
    val title: String,

    @SerializedName("url")
    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "favorite", defaultValue = "false")
    var favorite: Boolean = false
)