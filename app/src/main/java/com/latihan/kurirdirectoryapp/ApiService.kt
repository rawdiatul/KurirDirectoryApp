package com.latihan.kurirdirectoryapp

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("users")
    suspend fun getAllUsers(): Response<List<UserResponse>>
}
