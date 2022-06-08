package com.aditasha.myapplication.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("register")
    suspend fun register(
        @Body register: Register,
    ): Response<GeneralResponse>

    @POST("login")
    suspend fun login(
        @Body login: Login,
    ): Response<LoginResponse>

    @GET("stories")
    suspend fun stories(
        @Header("Authorization")
        token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("location") location: Int = 0
    ): Response<StoryResponse>

    @Multipart
    @POST("stories")
    suspend fun upload(
        @Header("Authorization")
        token: String,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part,
        @Part("lat") lat: Float? = null,
        @Part("lon") lon: Float? = null
    ): Response<GeneralResponse>
}