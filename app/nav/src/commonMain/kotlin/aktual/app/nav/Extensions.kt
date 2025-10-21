/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.nav

import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import logcat.logcat

internal fun <T : Any> NavHostController.debugNav(route: T, builder: NavOptionsBuilder.() -> Unit) {
  printBackStack()
  logcat.v("debugNavigate") { "debugNavigate to $route with builder" }
  navigate(route, navOptions(builder))
}

internal fun <T : Any> NavHostController.debugNav(route: T) {
  printBackStack()
  logcat.v("debugNavigate") { "debugNavigate to $route" }
  navigate(route)
}

@Suppress("RestrictedApi")
private fun NavHostController.printBackStack() {
  val backstack = currentBackStack.value
  val str = backstack.joinToString { it.destination.toString() }
  logcat.v("printBackStack") { "backstack = $str" }
}
