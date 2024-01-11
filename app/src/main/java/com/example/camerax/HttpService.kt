package com.example.camerax

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface  ApiService {

    @GET("models")
    suspend fun getModels() : Models
    @Multipart
    @POST("upload")
    suspend fun uploadImage(@Part image : MultipartBody.Part) : UploadResponse
}

data class Models(
    val models: List<String>
)
data class UploadResponse(
    val model : String,
    val image_name : String
)


object HttpService {

    private const val URL = "https://8bfb-200-201-27-132.ngrok-free.app"


    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api : ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

}