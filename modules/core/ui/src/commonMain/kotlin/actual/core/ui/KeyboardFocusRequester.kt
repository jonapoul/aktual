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
package actual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Returns a [FocusRequester] which shows the system keyboard after the given [keyboardDelay] period. Make sure to
 * pass this into the [androidx.compose.ui.focus.focusRequester] modifier on the TextField in question.
 */
@Composable
fun keyboardFocusRequester(
  keyboard: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
  keyboardDelay: Duration = KEYBOARD_SHOW_DELAY,
): FocusRequester {
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(focusRequester) {
    focusRequester.requestFocus()
    delay(keyboardDelay)
    keyboard?.show()
  }

  return focusRequester
}

private val KEYBOARD_SHOW_DELAY = 1000.milliseconds
