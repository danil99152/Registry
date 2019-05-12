package com.example.registry

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface JSONPlaceHolderApi {

    @get:GET("/posts")
    val allPosts: Call<List<Post>>

    @GET("/posts/{id}")
    fun getPostWithID(@Path("id") id: Int): Call<Post>

    @GET("/posts")
    fun getPostOfUser(@Query("userId") id: Int): Call<List<Post>>

    @POST("/posts")
    fun postData(@Body data: Post): Call<Post>
}