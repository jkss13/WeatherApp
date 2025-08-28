package com.ifpe.weatherapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.ifpe.weatherapp.api.WeatherService
import com.ifpe.weatherapp.db.fb.FBDatabase
import com.ifpe.weatherapp.db.local.LocalDatabase
import com.ifpe.weatherapp.model.MainViewModel
import com.ifpe.weatherapp.model.MainViewModelFactory
import com.ifpe.weatherapp.monitor.ForecastMonitor
import com.ifpe.weatherapp.repo.Repository
import com.ifpe.weatherapp.ui.nav.BottomNavBar
import com.ifpe.weatherapp.ui.nav.BottomNavItem
import com.ifpe.weatherapp.ui.nav.CityDialog
import com.ifpe.weatherapp.ui.nav.MainNavHost
import com.ifpe.weatherapp.ui.nav.Route
import com.ifpe.weatherapp.ui.theme.WeatherAppTheme
import java.util.function.Consumer
import android.Manifest

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val fbDB = remember { FBDatabase() }
            val weatherService = remember { WeatherService() }
            val forecastMonitor = remember { ForecastMonitor(this@MainActivity) }
            val currentUser = Firebase.auth.currentUser
            val dbName = "weather_db_${currentUser?.uid ?: "guest"}"
            val localDB = remember { LocalDatabase(this@MainActivity, dbName) }
            val repository = remember { Repository(fbDB, localDB) }
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(repository, weatherService, this@MainActivity)
            )

            val navController = rememberNavController()
            var showDialog by remember { mutableStateOf(false) }
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val showButton = currentRoute == getRouteName(Route.List)

            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    // handle permission result if needed
                }
            )

            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { intent ->
                    val name = intent.getStringExtra("city")
                    if (name != null) {
                        val city = viewModel.cities.find { it.name == name }
                        if (city != null) {
                            viewModel.city = city
                            viewModel.page = Route.Home
                        }
                    }
                }

                onDispose {
                    // cleanup
                }
            }

            WeatherAppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                val name = viewModel.user?.name ?: "[não logado]"
                                Text("Bem-vindo/a! $name")
                            },
                            actions = {
                                IconButton(onClick = {
                                    Firebase.auth.signOut()
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = "Sair"
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        val items = listOf(
                            BottomNavItem.HomeButton,
                            BottomNavItem.ListButton,
                            BottomNavItem.MapButton,
                        )
                        BottomNavBar(viewModel = viewModel, items = items)
                    },
                    floatingActionButton = {
                        if (showButton) {
                            FloatingActionButton(onClick = { showDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Adicionar")
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        LaunchedEffect(Unit) {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        MainNavHost(navController = navController, viewModel = viewModel)
                    }

                    LaunchedEffect(viewModel.page) {
                        navController.navigate(getRouteName(viewModel.page)) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }

                if (showDialog) {
                    CityDialog(
                        onDismiss = { showDialog = false },
                        onConfirm = { city ->
                            if (city.isNotBlank()) {
                                viewModel.add(city)
                            }
                            showDialog = false
                        }
                    )
                }
            }
        }
    }
}

fun getRouteName(route: Route): String {
    return when (route) {
        is Route.Home -> "Route.Home"
        is Route.List -> "Route.List"
        is Route.Map -> "Route.Map"
        else -> throw IllegalArgumentException("Rota desconhecida: $route")
    }
}