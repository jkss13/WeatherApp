package com.ifpe.weatherapp.model

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.ifpe.weatherapp.api.WeatherService
import com.ifpe.weatherapp.api.toForecast
import com.ifpe.weatherapp.api.toWeather
import com.ifpe.weatherapp.repo.Repository
import com.ifpe.weatherapp.ui.nav.Route
import com.ifpe.weatherapp.monitor.ForecastMonitor
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val repository: Repository,
    private val service: WeatherService,
    private val monitor: ForecastMonitor
) : ViewModel() {

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
        viewModelScope.launch(Dispatchers.Main) {
            repository.user.collect { user ->
                _user.value = user.copy()
            }
        }

        viewModelScope.launch(Dispatchers.Main) {
            repository.cities.collect { list ->
                val names = list.map { it.name }
                val newCities = list.filter { it.name !in _cities.keys }
                val oldCities = list.filter { it.name in _cities.keys }
                _cities.keys.removeIf { it !in names }
                newCities.forEach { _cities[it.name] = it }
                oldCities.forEach { refresh(it) }
            }
        }
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

    fun add(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val location = service.getLocation(name) ?: return@launch
        repository.add(City(name = name, location = location))
    }

    fun add(location: LatLng) = viewModelScope.launch(Dispatchers.IO) {
        val name = service.getName(location.latitude, location.longitude) ?: return@launch
        repository.add(City(name = name, location = location))
    }

    fun loadWeather(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val weather = service.getWeather(name)?.toWeather()
        _cities[name]?.let { refresh(it.copy(weather = weather)) }
    }

    fun loadForecast(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val forecast = service.getForecast(name)?.toForecast()
        _cities[name]?.let { refresh(it.copy(forecast = forecast)) }
    }

    fun loadBitmap(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val city = _cities[name]
        val bitmap = city?.weather?.imgUrl?.let { service.getBitmap(it) }
        city?.let { refresh(it.copy(weather = it.weather?.copy(bitmap = bitmap))) }
    }

    fun cleanupMonitoring() {
        monitor.cancelAll()
    }

    suspend fun refresh(city: City) = withContext(Dispatchers.Main) {
        val oldCity = _cities[city.name]
        _cities.remove(city.name)
        _cities[city.name] = city.copy(
            weather = city.weather ?: oldCity?.weather,
            forecast = city.forecast ?: oldCity?.forecast,
        )
        if (_city.value?.name == city.name) {
            _city.value = _cities[city.name]
        }
        monitor.updateCity(_cities[city.name]!!)
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