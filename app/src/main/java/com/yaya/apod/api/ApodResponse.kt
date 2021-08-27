package com.yaya.apod.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ApodResponse(

    @field:SerializedName("copyright")
    @field:Expose
    val copyright: String,

    @field:SerializedName("date")
    val date: String,

    @field:SerializedName("explanation")
    val explanation: String,

    @field:SerializedName("hdurl")
    @Expose
    val hdUrl: String,

    @field:SerializedName("media_type")
    val mediaType: String,

    @field:SerializedName("service_version")
    val serviceVersion: String,

    @field:SerializedName("title")
    val title: String,

    @field:SerializedName("url")
    val url: String
)