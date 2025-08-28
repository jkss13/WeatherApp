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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ifpe.weatherapp.model.MainViewModel
import com.ifpe.weatherapp.ui.nav.BottomNavBar
import com.ifpe.weatherapp.ui.nav.BottomNavItem
import com.ifpe.weatherapp.ui.nav.MainNavHost
import com.ifpe.weatherapp.ui.nav.CityDialog
import com.ifpe.weatherapp.ui.theme.WeatherAppTheme
import com.ifpe.weatherapp.ui.nav.Route
import android.Manifest
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.ifpe.weatherapp.api.WeatherService
import com.ifpe.weatherapp.db.fb.FBDatabase
import com.ifpe.weatherapp.model.MainViewModelFactory
import com.ifpe.weatherapp.monitor.ForecastMonitor
import java.util.function.Consumer

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val fbDB = remember { FBDatabase() }
            val weatherService = remember { WeatherService() }
            val forecastMonitor = remember { ForecastMonitor(this@MainActivity) }

            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(fbDB, weatherService, this@MainActivity)
            )
            val navController = rememberNavController()
            var showDialog by remember { mutableStateOf(false) }
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Usando a função não-composable para obter o nome da rota
            val showButton = currentRoute == getRouteName(Route.List)

            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    // Handle permission result if needed
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
                    // Cleanup
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
                        // Usando a função não-composable para navegação
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

// Função auxiliar NÃO-COMPOSABLE para obter o nome da rota
fun getRouteName(route: Route): String {
    return when (route) {
        is Route.Home -> "Route.Home"
        is Route.List -> "Route.List"
        is Route.Map -> "Route.Map"
        else -> throw IllegalArgumentException("Rota desconhecida: $route")
    }
}