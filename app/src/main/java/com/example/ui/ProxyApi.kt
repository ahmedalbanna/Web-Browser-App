package com.example.ui

import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeonodeResponse(
    val data: List<GeonodeProxy>
)

@JsonClass(generateAdapter = true)
data class GeonodeProxy(
    val ip: String,
    val port: String,
    val country: String?,
    val latency: Double?
)

interface ProxyApi {
    @GET("api/proxy-list?limit=30&page=1&sort_by=lastChecked&sort_type=desc&protocols=http,https")
    suspend fun getProxies(): GeonodeResponse
}

object ProxyApiClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://proxylist.geonode.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: ProxyApi = retrofit.create(ProxyApi::class.java)
}
