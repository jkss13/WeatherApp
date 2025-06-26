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
    NavHost(navController, startDestination = Route.Home) {
        composable<Route.Home> {
            HomePage(viewModel = viewModel)
        }
        composable<Route.List> {
            ComposableListPage(viewModel = viewModel)
        }
        composable<Route.Map> {
            ComposableMapPage(viewModel = viewModel)
        }
    }
}