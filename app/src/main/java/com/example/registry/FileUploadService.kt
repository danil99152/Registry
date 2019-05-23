package com.example.registry

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileUploadService {
    @Multipart
    @POST("upload")
    fun upload(
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>
}