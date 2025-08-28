package com.ifpe.weatherapp.api

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherService {
    private var weatherAPI: WeatherServiceAPI

    init {
        val retrofitAPI = Retrofit.Builder()
            .baseUrl(WeatherServiceAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherAPI = retrofitAPI.create(WeatherServiceAPI::class.java)
    }

    suspend fun getName(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        search("$lat,$lng")?.name // retorno
    }

    suspend fun getLocation(name: String): LatLng? = withContext(Dispatchers.IO) {
        val location = search(name)
        if (location != null && location.lat != null && location.lon != null) {
            LatLng(location.lat!!, location.lon!!)
        } else {
            null
        }
    }

    private fun search(query: String): APILocation? {
        val call: Call<List<APILocation>?> = weatherAPI.search(query)
        return try {
            val response: Response<List<APILocation>?> = call.execute()
            val apiLoc = response.body()
            if (!apiLoc.isNullOrEmpty()) apiLoc[0] else null
        } catch (e: Exception) {
            Log.w("WeatherApp WARNING", "" + e.message)
            null
        }
    }

    suspend fun getWeather(name: String): APICurrentWeather? = withContext(Dispatchers.IO) {
        val call: Call<APICurrentWeather?> = weatherAPI.weather(name)
        try {
            call.execute().body() // retorno
        } catch (e: Exception) {
            Log.w("WeatherApp WARNING", "" + e.message)
            null
        }
    }

    suspend fun getForecast(name: String): APIWeatherForecast? = withContext(Dispatchers.IO) {
        val call: Call<APIWeatherForecast?> = weatherAPI.forecast(name)
        try {
            call.execute().body() // retorno
        } catch (e: Exception) {
            Log.w("WeatherApp WARNING", "" + e.message)
            null
        }
    }

    suspend fun getBitmap(imgUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            Picasso.get().load(imgUrl).get() // retorno
        } catch (e: Exception) {
            Log.w("WeatherApp WARNING", "" + e.message)
            null
        }
    }
}