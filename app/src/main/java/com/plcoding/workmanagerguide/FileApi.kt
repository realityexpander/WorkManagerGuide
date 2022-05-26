package com.plcoding.workmanagerguide

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET

interface FileApi {

    @GET("/tesla/assets/img/hero-mobile.jpg")
    suspend fun downloadImage(): Response<ResponseBody>

    companion object {
        val instance by lazy {
            Retrofit.Builder()
                .baseUrl("https://realityexpander.github.io")
                .build()
                .create(FileApi::class.java)
        }
    }
}