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
package aktual.core.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler as AndroidBackHandler

@Composable
actual fun BackHandler(
  enabled: Boolean,
  onBack: () -> Unit,
) = AndroidBackHandler(enabled, onBack)

@Composable
actual fun appCloser(): AppCloser {
  val context = LocalContext.current
  return remember(context) {
    AndroidAppCloser(
      activity = { context as? Activity ?: error("$context isn't an activity?") },
    )
  }
}

@Immutable
private class AndroidAppCloser(private val activity: () -> Activity) : AppCloser {
  override operator fun invoke() = activity().finish()
}
