package com.bluelock.moj.di

import com.bluelock.moj.models.MojVideo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DownloadAPIInterface {
    @GET("/instagram.php")
    fun getMojVideos(@Query("video") videoUrl: String?): Call<MojVideo>
}