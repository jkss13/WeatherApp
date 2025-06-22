package com.ifpe.weatherapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ifpe.weatherapp.HomePage
import com.ifpe.weatherapp.ComposableListPage
import com.ifpe.weatherapp.ComposableMapPage

@Composable
fun MainNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Route.Home) {
        composable<Route.Home> { HomePage()  }
        composable<Route.List> { ComposableListPage()  }
        composable<Route.Map>  { ComposableMapPage()   }
    }
}
