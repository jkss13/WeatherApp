package com.ifpe.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ifpe.weatherapp.model.MainViewModel

//@Preview(showBackground = true)
@Composable
fun HomePage(viewModel: MainViewModel) {
    Column {
        if (viewModel.city == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Blue)
                    .wrapContentSize(Alignment.Center)
            ) {
                Text(
                    text = "Selecione uma cidade!",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp
                )
            }
        } else {
            Row {
                AsyncImage( // Substitui o Icon
                    model = viewModel.city?.weather?.imgUrl,
                    modifier = Modifier.size(100.dp),
                    error = painterResource(id = R.drawable.loading),
                    contentDescription = "Imagem"
                )
                Column {
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = viewModel.city?.name ?: "Selecione uma cidade...",
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = viewModel.city?.weather?.desc ?: "...",
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "Temp: " + viewModel.city?.weather?.temp + "℃",
                        fontSize = 22.sp
                    )
                }
            }
            LaunchedEffect(viewModel.city!!.name) {
                if (viewModel.city!!.forecast == null ||
                    viewModel.city!!.forecast!!.isEmpty()
                ) {
                    viewModel.loadForecast(viewModel.city!!.name)
                }
            }
            if (viewModel.city?.forecast != null) {
                LazyColumn {
                    items(viewModel.city!!.forecast!!) { forecast ->
                        ForecastItem(forecast, onClick = { })
                    }
                }
            }
        }
    }
}