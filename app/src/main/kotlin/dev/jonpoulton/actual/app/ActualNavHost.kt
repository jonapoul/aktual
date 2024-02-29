package dev.jonpoulton.actual.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun ActualNavHost() {
  val navController = rememberNavController()
  NavHost(
    navController = navController,
    startDestination = "",
  ) {
    // TBC
  }
}
