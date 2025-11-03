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
  logcat.v(TAG) { "Nav to $route with builder - backStack=[$backStack]" }
  navigate(route, navOptions(builder))
}

internal fun <T : Any> NavHostController.debugNav(route: T) {
  logcat.v(TAG) { "Nav to $route - backStack=[$backStack]" }
  navigate(route)
}

private val NavHostController.backStack get() = currentBackStack.value.joinToString { entry ->
  "route=${entry.destination.route},args=${entry.arguments}"
}

private const val TAG = "debugNavigate"
