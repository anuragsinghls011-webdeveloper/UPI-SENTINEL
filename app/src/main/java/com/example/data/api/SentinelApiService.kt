package com.example.data.api

import com.example.data.model.CloudAnalysisRequest
import com.example.data.model.CloudAnalysisResponse
import com.example.data.model.CybercrimeThreatRecord
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface SentinelApiService {

    @POST("api/transactions/analyze")
    suspend fun analyzeTransaction(
        @Body request: CloudAnalysisRequest
    ): Response<CloudAnalysisResponse>

    @GET("api/threats/cybercrime-feed")
    suspend fun getCybercrimeThreatFeed(): Response<List<CybercrimeThreatRecord>>

    companion object {
        // Fallback default url if not provided in buildConfig
        private const val DEFAULT_BASE_URL = "http://10.0.2.2:5000/"

        fun create(baseUrl: String = DEFAULT_BASE_URL): SentinelApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(SentinelApiService::class.java)
        }
    }
}
