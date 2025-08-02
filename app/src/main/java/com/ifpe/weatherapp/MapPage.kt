package com.ifpe.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import com.ifpe.weatherapp.model.MainViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

//@Preview(showBackground = true)
@Composable
fun ComposableMapPage(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    val cityList = viewModel.cities
    val camPosState = rememberCameraPositionState()
    val context = LocalContext.current

    GoogleMap (
        modifier = modifier.fillMaxSize(),
        onMapClick = {
            viewModel.add(location = it)
        },
        cameraPositionState = camPosState
    )  {
        viewModel.cities.forEach {
            if (it.location != null) {
                LaunchedEffect(it.name) {
                    if (it.weather == null) {
                        viewModel.loadWeather(it.name)
                    }
                }
                LaunchedEffect(it.weather) {
                    if (it.weather != null && it.weather!!.bitmap == null) {
                        viewModel.loadBitmap(it.name)
                    }
                }
                val image = it.weather?.bitmap ?:
                getDrawable(context, R.drawable.loading)!!
                    .toBitmap()
                val marker = BitmapDescriptorFactory
                    .fromBitmap(image.scale(120,120))
                Marker( state = MarkerState(position = it.location),
                    title = it.name,
                    icon = marker,
                    snippet = it.weather?.desc?:"Carregando..."
                )
            }
        }
    }
}