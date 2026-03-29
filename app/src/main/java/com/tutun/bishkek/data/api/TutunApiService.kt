package com.tutun.bishkek.data.api

import com.tutun.bishkek.data.model.AqiResponse
import com.tutun.bishkek.data.model.OwmResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TutunApiService {

    // AQICN — Live AQI for Bishkek by coordinates
    @GET("feed/geo:42.8746;74.5698/")
    suspend fun getBishkekAqi(
        @Query("token") token: String
    ): AqiResponse

    // AQICN — AQI for a specific district by its coordinates
    @GET("feed/geo:{lat};{lon}/")
    suspend fun getDistrictAqi(
        @Path("lat") lat: String,
        @Path("lon") lon: String,
        @Query("token") token: String
    ): AqiResponse

    // OpenWeatherMap One Call 3.0 — full weather data
    @GET("data/3.0/onecall")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String = "minutely",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): OwmResponse
}


