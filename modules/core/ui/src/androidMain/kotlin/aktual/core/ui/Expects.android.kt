/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
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
