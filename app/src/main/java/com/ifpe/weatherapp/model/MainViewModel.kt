package com.ifpe.weatherapp.model

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.ifpe.weatherapp.api.WeatherService
import com.ifpe.weatherapp.api.toForecast
import com.ifpe.weatherapp.api.toWeather
import com.ifpe.weatherapp.repo.Repository
import com.ifpe.weatherapp.ui.nav.Route
import com.ifpe.weatherapp.monitor.ForecastMonitor
import android.content.Context

class MainViewModel(
    private val repository: Repository,
    private val service: WeatherService,
    private val monitor: ForecastMonitor
) : ViewModel(), Repository.Listener {

    private var _city = mutableStateOf<City?>(null)
    var city: City?
        get() = _city.value
        set(tmp) { _city.value = tmp?.copy() }

    private val _cities = mutableStateMapOf<String, City>()
    val cities: List<City>
        get() = _cities.values.toList()

    private val _user = mutableStateOf<User?>(null)
    val user: User?
        get() = _user.value

    private var _page = mutableStateOf<Route>(Route.Home)
    var page: Route
        get() = _page.value
        set(tmp) { _page.value = tmp }

    init {
        repository.setListener(this)
    }

    fun update(city: City) {
        repository.update(city)
        monitor.updateCity(city)
    }

    fun remove(city: City) {
        repository.remove(city)
        monitor.cancelCity(city)
    }

    fun add(name: String, location: LatLng? = null) {
        val newCity = City(name = name, location = location)
        repository.add(newCity)
    }

    override fun onUserLoaded(user: User) {
        _user.value = user
    }

    override fun onUserSignOut() {
        monitor.cancelAll()
    }

    override fun onCityAdded(city: City) {
        _cities[city.name] = city
        monitor.updateCity(city)
    }

    override fun onCityUpdated(city: City) {
        val oldCity = _cities[city.name]
        _cities.remove(city.name)
        val newCity = city.copy(
            weather = oldCity?.weather,
            forecast = oldCity?.forecast
        )
        _cities[city.name] = newCity

        monitor.updateCity(newCity)

        if (_city.value?.name == city.name) {
            _city.value = _cities[city.name]
        }
    }

    override fun onCityRemoved(city: City) {
        val removedCity = _cities[city.name]
        _cities.remove(city.name)

        removedCity?.let { monitor.cancelCity(it) }
        if (_city.value?.name == city.name) {
            _city.value = null
        }
    }

    fun add(name: String) {
        service.getLocation(name) { lat, lng ->
            if (lat != null && lng != null) {
                val newCity = City(name = name, location = LatLng(lat, lng))
                repository.add(newCity)
            }
        }
    }

    fun add(location: LatLng) {
        service.getName(location.latitude, location.longitude) { name ->
            if (name != null) {
                val newCity = City(name = name, location = location)
                repository.add(newCity)
            }
        }
    }

    fun loadWeather(name: String) {
        service.getWeather(name) { apiWeather ->
            val newCity = _cities[name]!!.copy(weather = apiWeather?.toWeather())
            _cities.remove(name)
            _cities[name] = newCity
        }
    }

    fun loadForecast(name: String) {
        service.getForecast(name) { apiForecast ->
            val newCity = _cities[name]!!.copy(forecast = apiForecast?.toForecast())
            _cities.remove(name)
            _cities[name] = newCity
            city = if (city?.name == name) newCity else city
        }
    }

    fun loadBitmap(name: String) {
        val city = _cities[name]
        service.getBitmap(city?.weather!!.imgUrl) { bitmap ->
            val newCity = city.copy(
                weather = city.weather?.copy(
                    bitmap = bitmap
                )
            )
            _cities.remove(name)
            _cities[name] = newCity
        }
    }

    fun cleanupMonitoring() {
        monitor.cancelAll()
    }
}

class MainViewModelFactory(
    private val repository: Repository,
    private val service: WeatherService,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val monitor = ForecastMonitor(context)
            return MainViewModel(repository, service, monitor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}