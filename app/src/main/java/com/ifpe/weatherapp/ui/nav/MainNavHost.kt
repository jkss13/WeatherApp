package com.ifpe.weatherapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ifpe.weatherapp.HomePage
import com.ifpe.weatherapp.ComposableListPage
import com.ifpe.weatherapp.ComposableMapPage
import com.ifpe.weatherapp.model.MainViewModel

@Composable
fun MainNavHost(navController: NavHostController, viewModel: MainViewModel) {
    NavHost(navController, startDestination = getRouteName(Route.Home)) {
        composable(getRouteName(Route.Home)) {
            HomePage(viewModel = viewModel)
        }
        composable(getRouteName(Route.List)) {
            ComposableListPage(viewModel = viewModel)
        }
        composable(getRouteName(Route.Map)) {
            ComposableMapPage(viewModel = viewModel)
        }
    }
}

// Função auxiliar para obter o nome da rota (deve ser a mesma usada no MainActivity)
fun getRouteName(route: Route): String {
    return when (route) {
        is Route.Home -> "Route.Home"
        is Route.List -> "Route.List"
        is Route.Map -> "Route.Map"
        else -> throw IllegalArgumentException("Rota desconhecida: $route")
    }
}