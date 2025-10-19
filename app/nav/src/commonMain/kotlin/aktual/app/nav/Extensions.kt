/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
