package com.example.cwc.cloudinary

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// This object sets up Retrofit for Cloudinary.
object CloudinaryService {
    private const val BASE_URL = "https://api.cloudinary.com/"

    // Lazily create the Retrofit instance once, and reuse it throughout the app.
    val api: CloudinaryApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudinaryApi::class.java)
    }
}
