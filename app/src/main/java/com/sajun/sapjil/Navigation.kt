package com.sajun.sapjil

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sajun.sapjil.screen.MainScreen
import com.sajun.sapjil.screen.SensorTestScreen

object Route {
    const val MAIN = "main"
    const val TEST_SENSOR = "test_sensor"
}

@Composable
fun Navigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding),
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { slideInHorizontally { -it } },
        popExitTransition = { slideOutHorizontally { it } }
    ) {
        composable(Route.MAIN) {
            MainScreen(navController)
        }
        composable(Route.TEST_SENSOR) {
            SensorTestScreen(navController)
        }
    }
}